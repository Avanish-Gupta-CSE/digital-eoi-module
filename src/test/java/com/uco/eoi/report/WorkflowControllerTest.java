package com.uco.eoi.report;

import static org.assertj.core.api.Assertions.assertThat;

import com.uco.eoi.auth.StaffPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class WorkflowControllerTest {

    @Test
    @DisplayName("a maker cannot re-open a report: refused before the service is called")
    void reopen_byAMaker_isRefused() {
        // The service is null on purpose: a refusal must happen before it is reached.
        WorkflowController controller = new WorkflowController(null);
        StaffPrincipal maker = new StaffPrincipal("M1", "Maker", "1", "S1", "Officer",
                StaffPrincipal.MAKER);
        RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();

        String view = controller.reopen(5, "A reason that is long enough.", maker, attributes);

        assertThat(view).isEqualTo("redirect:/reports/5?error=notallowed");
        assertThat(attributes.getFlashAttributes()).containsKey("errorMessage");
    }

    @Test
    @DisplayName("with nobody signed in every workflow action is refused before the service is called")
    void everyAction_withNobodySignedIn_isRefused() {
        WorkflowController controller = new WorkflowController(null);
        RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();

        assertThat(controller.submit(5, null, attributes)).isEqualTo("redirect:/reports/5?error=notallowed");
        assertThat(controller.approve(5, null, attributes)).isEqualTo("redirect:/reports/5?error=notallowed");
        assertThat(controller.returnToMaker(5, "A reason that is long enough.", null, attributes))
                .isEqualTo("redirect:/reports/5?error=notallowed");
        assertThat(controller.reopen(5, "A reason that is long enough.", null, attributes))
                .isEqualTo("redirect:/reports/5?error=notallowed");
        assertThat(controller.syncFinacle(5, null, attributes)).isEqualTo("redirect:/reports/5?error=notallowed");
    }
}
