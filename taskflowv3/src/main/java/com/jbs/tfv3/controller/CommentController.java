package com.jbs.tfv3.controller;

import com.jbs.tfv3.dto.ApiResponse;
import com.jbs.tfv3.dto.CommentRequest;
import com.jbs.tfv3.dto.CommentResponse;
import com.jbs.tfv3.entity.Comment;
import com.jbs.tfv3.service.impl.CommentServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Autowired
    private CommentServiceImpl commentServiceImpl;

    // ---------------------------------------------------------------------
    @Operation(
        tags = "Comments",
        summary = "Create a new comment on a ticket",
        description = "Allows ADMIN or USER to add a comment to a specific ticket identified by its ID."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Comment created successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Ticket or user not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - only Admin or User can comment"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error creating comment"
        )
    })
    @PostMapping(
        value = "/tickets/{ticketId}/comments",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable Long ticketId,
            @RequestPart("comment") CommentRequest req,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        String authorEmail =
                SecurityContextHolder.getContext().getAuthentication().getName();

        Comment saved =
                commentServiceImpl.createComment(ticketId, authorEmail, req, files);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        201,
                        "Comment created successfully",
                        commentServiceImpl.toResponse(saved)
                ));
    }

    // ---------------------------------------------------------------------
    @Operation(
        tags = "Comments",
        summary = "Get all comments for a ticket",
        description = "Retrieves a list of comments associated with a specific ticket."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Comments retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Ticket not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error retrieving comments"
        )
    })
    @GetMapping("/tickets/{ticketId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @PathVariable Long ticketId) {

        logger.info("GET /tickets/{}/comments", ticketId);
        try {
            List<CommentResponse> list =
                    commentServiceImpl.getCommentsByTicket(ticketId);

            return ResponseEntity.ok(
                    new ApiResponse<>(200, "Comments retrieved successfully", list)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error retrieving comments", null));
        }
    }

    // ---------------------------------------------------------------------
    @Operation(
        tags = "Comments",
        summary = "Update a comment",
        description = "Allows ADMIN or the original comment author to update a comment."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Comment updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Comment not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error updating comment"
        )
    })
    @PatchMapping("/comments/{commentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest req) {

        logger.info("PATCH /comments/{}", commentId);
        String authorEmail =
                SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            Comment updated =
                    commentServiceImpl.updateComment(commentId, authorEmail, req);

            return ResponseEntity.ok(
                    new ApiResponse<>(200, "Comment updated successfully",
                            commentServiceImpl.toResponse(updated))
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(403, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error updating comment", null));
        }
    }

    // ---------------------------------------------------------------------
    @Operation(
        tags = "Comments",
        summary = "Delete a comment",
        description = "Allows ADMIN or the original comment author to delete a comment."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Comment deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Comment not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error deleting comment"
        )
    })
    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<ApiResponse<CommentResponse>> deleteComment(
            @PathVariable Long commentId) {

        logger.info("DELETE /comments/{}", commentId);
        String authorEmail =
                SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            Comment deleted =
                    commentServiceImpl.deleteComment(commentId, authorEmail);

            return ResponseEntity.ok(
                    new ApiResponse<>(200, "Comment deleted successfully",
                            commentServiceImpl.toResponse(deleted))
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(403, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error deleting comment", null));
        }
    }
}
