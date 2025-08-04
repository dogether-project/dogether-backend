package site.dogether.memberactivity.controller.v2;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.auth.resolver.Authenticated;
import site.dogether.common.controller.dto.response.ApiResponse;
import site.dogether.memberactivity.controller.v2.dto.response.GetMemberAllStatsResponseV2;
import site.dogether.memberactivity.service.MemberActivityServiceV2;

import static site.dogether.common.controller.dto.response.ApiResponse.success;

@RequiredArgsConstructor
@RequestMapping("/api/v1/my")
@RestController
public class MemberActivityControllerV2 {

    private final MemberActivityServiceV2 memberActivityServiceV2;

    @GetMapping("/activity")
    public ResponseEntity<ApiResponse<GetMemberAllStatsResponseV2>> getMemberAllStats(
            @Authenticated final Long memberId,
            @RequestParam final String sortBy,
            @RequestParam(required = false) final String status,
            @PageableDefault(size = 50) final Pageable pageable
    ) {
        final GetMemberAllStatsResponseV2 memberAllStats = memberActivityServiceV2.getMemberAllStats(memberId, sortBy, status, pageable);

        return ResponseEntity.ok(success(memberAllStats));
    }
}
