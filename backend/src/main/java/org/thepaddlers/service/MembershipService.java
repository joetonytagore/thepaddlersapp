package org.thepaddlers.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thepaddlers.model.Membership;
import org.thepaddlers.model.MembershipPlan;
import org.thepaddlers.model.StripeEvent;
import org.thepaddlers.repository.MembershipPlanRepository;
import org.thepaddlers.repository.MembershipRepository;
import org.thepaddlers.repository.StripeEventRepository;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
public class MembershipService {
    private final MembershipPlanRepository planRepo;
    private final MembershipRepository membershipRepo;
    private final StripeEventRepository stripeEventRepository;
    private final StripeService stripeService;

    @Autowired
    public MembershipService(MembershipPlanRepository planRepo, MembershipRepository membershipRepo, StripeEventRepository stripeEventRepository, StripeService stripeService) {
        this.planRepo = planRepo;
        this.membershipRepo = membershipRepo;
        this.stripeEventRepository = stripeEventRepository;
        this.stripeService = stripeService;
    }

    public MembershipPlan createPlan(MembershipPlan p) {
        return planRepo.save(p);
    }

    public Membership subscribeUser(Long orgId, Long planId, Long userId, String customerEmail) {
        // Minimal local-only subscribe flow, create Stripe subscription if stripePriceId available
        MembershipPlan plan = planRepo.findById(planId).orElseThrow();
        Membership m = new Membership();
        m.setOrgId(orgId);
        m.setUserId(userId);
        m.setPlanId(planId);
        m.setStatus("ACTIVE");
        // initialize credits from plan
        m.setCreditsRemaining(plan.getCreditsPerPeriod() != null ? plan.getCreditsPerPeriod() : 0);

        // If plan has stripePriceId and Stripe is configured, create customer and subscription
        if (plan.getStripePriceId() != null && !plan.getStripePriceId().isBlank()) {
            try {
                Map<String, Object> cust = stripeService.createCustomer(customerEmail);
                String customerId = (String) cust.get("id");
                Map<String, Object> sub = stripeService.createSubscription(customerId, plan.getStripePriceId());
                String subId = (String) sub.get("id");
                m.setStripeSubscriptionId(subId);
                // optionally set current period from subscription latest_invoice.period
                if (sub.get("current_period_start") instanceof Number && sub.get("current_period_end") instanceof Number) {
                    long s = ((Number) sub.get("current_period_start")).longValue();
                    long e = ((Number) sub.get("current_period_end")).longValue();
                    m.setCurrentPeriodStart(OffsetDateTime.ofInstant(java.time.Instant.ofEpochSecond(s), java.time.ZoneOffset.UTC));
                    m.setCurrentPeriodEnd(OffsetDateTime.ofInstant(java.time.Instant.ofEpochSecond(e), java.time.ZoneOffset.UTC));
                }
            } catch (Exception ex) {
                // log but allow local membership creation
                ex.printStackTrace();
            }
        }
        membershipRepo.save(m);
        return m;
    }

    // new: reconcile invoice events from Stripe to update membership state and credits
    public void handleInvoicePaid(String stripeSubscriptionId, long periodStartEpochSecs, long periodEndEpochSecs) {
        // find membership by stripeSubscriptionId
        membershipRepo.findAll().stream().filter(m -> stripeSubscriptionId.equals(m.getStripeSubscriptionId())).forEach(m -> {
            m.setCurrentPeriodStart(OffsetDateTime.ofInstant(java.time.Instant.ofEpochSecond(periodStartEpochSecs), java.time.ZoneOffset.UTC));
            m.setCurrentPeriodEnd(OffsetDateTime.ofInstant(java.time.Instant.ofEpochSecond(periodEndEpochSecs), java.time.ZoneOffset.UTC));
            // refill credits from plan
            MembershipPlan plan = planRepo.findById(m.getPlanId()).orElse(null);
            if (plan != null) {
                m.setCreditsRemaining(plan.getCreditsPerPeriod() != null ? plan.getCreditsPerPeriod() : 0);
            }
            m.setStatus("ACTIVE");
            membershipRepo.save(m);
        });
    }

    public void handleInvoiceFailed(String stripeSubscriptionId) {
        membershipRepo.findAll().stream().filter(m -> stripeSubscriptionId.equals(m.getStripeSubscriptionId())).forEach(m -> {
            m.setStatus("PAST_DUE");
            membershipRepo.save(m);
        });
    }

    public boolean markStripeEventProcessed(String eventId, String payload){
        if (stripeEventRepository.findByEventId(eventId).isPresent()) return false;
        StripeEvent se = new StripeEvent();
        se.setEventId(eventId);
        se.setPayload(payload);
        se.setProcessed(false);
        stripeEventRepository.save(se);
        return true;
    }
}
