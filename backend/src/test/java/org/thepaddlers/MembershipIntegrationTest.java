package org.thepaddlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.thepaddlers.model.MembershipPlan;
import org.thepaddlers.repository.MembershipPlanRepository;
import org.thepaddlers.service.MembershipService;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
public class MembershipIntegrationTest {
    @Autowired
    MembershipService membershipService;

    @Autowired
    MembershipPlanRepository planRepository;

    @Test
    void createPlanAndSubscribe() {
        MembershipPlan p = new MembershipPlan();
        p.setName("Monthly");
        p.setOrgId(1L);
        p.setPriceCents(2000L);
        p.setInterval("month");
        MembershipPlan saved = membershipService.createPlan(p);
        assertThat(saved.getId()).isNotNull();

        var m = membershipService.subscribeUser(1L, saved.getId(), 1L, "demo@paddlers.test");
        assertThat(m).isNotNull();
        assertThat(m.getStatus()).isEqualTo("ACTIVE");
    }
}
