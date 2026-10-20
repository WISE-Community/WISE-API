package org.wise.portal.service.teacherpresentation;

/**
 * A student response selected for presentation under an idea category. Dialog Guidance work is
 * additive (each save contains the whole chat), so a response is identified by workgroupId and
 * responseTimestamp (the time of the chat message). responseTimestamp is null for single response
 * work, which is identified by studentWorkId. text is a snapshot of the response.
 */
public record IdeasResponse(Integer studentWorkId, Long workgroupId, Long responseTimestamp,
    String text) {
}
