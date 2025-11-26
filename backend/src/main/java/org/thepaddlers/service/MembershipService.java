package org.thepaddlers.service;

import org.springframework.stereotype.Service;
import org.thepaddlers.model.Membership;
import org.thepaddlers.model.MembershipPlan;
import org.thepaddlers.model.StripeEvent;
import org.thepaddlers.repository.MembershipPlanRepository;
import org.thepaddlers.repository.MembershipRepository;
import org.thepaddlers.repository.StripeEventRepository;

@Service
public class MembershipService {
    private final MembershipPlanRepository planRepo;
    private final MembershipRepository membershipRepo;
    private final StripeEventRepository stripeEventRepository;

    public MembershipService(MembershipPlanRepository planRepo, MembershipRepository membershipRepo, StripeEventRepository stripeEventRepository) {
        this.planRepo = planRepo;
        this.membershipRepo = membershipRepo;
        this.stripeEventRepository = stripeEventRepository;
    }

    public MembershipPlan createPlan(MembershipPlan p) {
        return planRepo.save(p);
    }

    public Membership subscribeUser(Long orgId, Long planId, Long userId, String customerEmail) {
        // Minimal local-only subscribe flow (no Stripe integration active)
        MembershipPlan plan = planRepo.findById(planId).orElseThrow();
        Membership m = new Membership();
        m.setOrgId(orgId);
        m.setUserId(userId);
        m.setPlanId(planId);
        m.setStatus("ACTIVE");
        membershipRepo.save(m);
        return m;
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
