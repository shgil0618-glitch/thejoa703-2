/*
 * package com.thejoa703.controller;
 * 
 * import java.util.List; import org.springframework.stereotype.Controller;
 * import org.springframework.ui.Model; import
 * org.springframework.web.bind.annotation.GetMapping; import
 * org.springframework.web.bind.annotation.PostMapping; import
 * org.springframework.web.bind.annotation.RequestMapping; import
 * org.springframework.web.bind.annotation.RequestParam;
 * 
 * import com.thejoa703.dto.CommentDto; import com.thejoa703.dto.ReviewDto;
 * import com.thejoa703.service.CommentService; import
 * com.thejoa703.service.ReviewService;
 * 
 * @Controller
 * 
 * @RequestMapping("/reviews") // 모든 주소를 /reviews로 통합 public class
 * CommentController {
 * 
 * private final CommentService commentService; private final ReviewService
 * reviewService;
 * 
 * public CommentController(CommentService commentService, ReviewService
 * reviewService) { this.commentService = commentService; this.reviewService =
 * reviewService; }
 * 
 * // 1. 리뷰 및 댓글 목록 보기
 * 
 * @GetMapping("/list") public String reviewList(@RequestParam int recipeId,
 * Model model) { // 이 메서드가 실행되어야 합니다! List<ReviewDto> reviewList =
 * reviewService.getReviewsByRecipeId(recipeId); // 혹은
 * reviewService.getReviewList(recipeId);
 * 
 * System.out.println("가져온 리뷰 개수: " + (reviewList != null ? reviewList.size() :
 * "null"));
 * 
 * model.addAttribute("reviewList", reviewList); model.addAttribute("recipeId",
 * recipeId); // ... 평점 코드 생략 return "reviews/list"; }
 * 
 * // 2. 댓글 등록 (HTML의 /reviews/add와 매칭)
 * 
 * @PostMapping("/add") public String addComment(CommentDto dto, @RequestParam
 * int recipeId) { dto.setAppUserId(1); // 임시 사용자 ID if (dto.getParentId() ==
 * null) { dto.setParentId(0); } commentService.addComment(dto); return
 * "redirect:/reviews/list?recipeId=" + recipeId; }
 * 
 * // 3. 댓글 삭제 (HTML의 /reviews/deleteComment와 매칭)
 * 
 * @PostMapping("/deleteComment") public String deleteComment(@RequestParam int
 * commentId, @RequestParam int recipeId) {
 * commentService.deleteComment(commentId); return
 * "redirect:/reviews/list?recipeId=" + recipeId; }
 * 
 * // 4. 댓글 수정 폼 이동 (HTML의 /reviews/editComment와 매칭)
 * 
 * @GetMapping("/editComment") public String editCommentForm(@RequestParam int
 * commentId, @RequestParam int recipeId, Model model) { CommentDto dto =
 * commentService.getCommentById(commentId); model.addAttribute("comment", dto);
 * model.addAttribute("recipeId", recipeId); return "reviews/editComment"; // 수정
 * 페이지 HTML 필요 }
 * 
 * // 5. 답글(대댓글) 폼 이동 (HTML의 /reviews/reply와 매칭)
 * 
 * @GetMapping("/reply") public String replyForm(@RequestParam int parentId,
 * 
 * @RequestParam int reviewId,
 * 
 * @RequestParam int recipeId, Model model) { model.addAttribute("parentId",
 * parentId); model.addAttribute("reviewId", reviewId);
 * model.addAttribute("recipeId", recipeId); return "reviews/replyComment"; //
 * 답글 페이지 HTML 필요 } }
 */