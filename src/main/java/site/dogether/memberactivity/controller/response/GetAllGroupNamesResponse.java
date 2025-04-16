package site.dogether.memberactivity.controller.response;

import java.util.List;

public record GetAllGroupNamesResponse(List<GroupNameResponse> groups) {

    public record GroupNameResponse(Long id, String name) {
    }
}
