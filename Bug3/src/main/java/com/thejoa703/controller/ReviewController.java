package com.thejoa703.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.thejoa703.dao.ReviewDao;
import com.thejoa703.dto.AppUserDto;
import com.thejoa703.dto.CommentDto;
import com.thejoa703.dto.ReviewDto;
import com.thejoa703.security.CustomUserDetails;
import com.thejoa703.service.AiApiService;
import com.thejoa703.service.AppUserService;
import com.thejoa703.service.CommentService;
import com.thejoa703.service.ReviewService;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired ReviewService reviewService;
    @Autowired CommentService commentService;
    @Autowired AppUserService userService;
    @Autowired AiApiService aiApiService;
    @Autowired ReviewDao reviewDao;

    public ReviewController(ReviewService reviewService, CommentService commentService) {
        this.reviewService = reviewService;
        this.commentService = commentService;
    }

 // 1. 리뷰 목록
    @GetMapping("/list")
    public String reviewList(@RequestParam int recipeId, 
                             @RequestParam(value = "page", defaultValue = "1") int page, 
                             Model model) {
        
        // 현재 페이지의 리뷰 목록 가져오기
        List<ReviewDto> reviews = reviewService.getReviewsByRecipeId(recipeId, page);
        
        if (reviews != null) {
            for (ReviewDto review : reviews) {
                review.setComments(commentService.getComments(review.getReviewId()));
            }
        }
        
        // ⭐ 전체 페이지 수 계산 로직 추가
        int totalReviews = reviewService.getReviewCount(recipeId);
        int totalPages = (int) Math.ceil((double) totalReviews / 5); // 한 페이지당 5개 기준

        model.addAttribute("reviewList", reviews);
        model.addAttribute("avgRating", reviewService.getAverageRating(recipeId));
        model.addAttribute("recipeId", recipeId);
        
        // ⭐ 페이징을 위해 모델에 추가
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        
     // ⭐ 추가: 전체 리뷰 개수를 모델에 직접 담습니다.
        model.addAttribute("totalReviewCount", totalReviews);
        
        return "reviews/list";
    }

    // 2. 리뷰 수정 페이지 이동
    @GetMapping("/edit")
    public String editReviewForm(@RequestParam int reviewId, Model model) {
        ReviewDto review = reviewService.getReviewById(reviewId);
        model.addAttribute("review", review);
        return "reviews/edit";
    }

    // 3. 리뷰 수정 실행
    @PostMapping("/edit")
    public String editReview(ReviewDto dto) {
        reviewService.updateReview(dto);
        return "redirect:/reviews/list?recipeId=" + dto.getRecipeId();
    }

    // 4. 댓글 수정 페이지 이동
    @GetMapping("/editComment")
    public String editCommentForm(@RequestParam int commentId, @RequestParam int recipeId, Model model) {
        model.addAttribute("comment", commentService.getCommentById(commentId));
        model.addAttribute("recipeId", recipeId);
        return "reviews/editComment";
    }

 // 5. 댓글 수정 실행
    @PostMapping("/editComment")
    public String editCommentPost(@ModelAttribute CommentDto dto, @RequestParam int recipeId, Authentication authentication) {
        // 1. 현재 로그인한 사용자 정보 가져오기
        AppUserDto loginUser = getLoginUser(authentication);
        
        // 2. DB에서 실제 댓글 정보를 가져와서 작성자 확인 (서비스에 getCommentById가 있다고 가정)
        CommentDto originalComment = commentService.getCommentById(dto.getCommentId());

        if (loginUser == null) return "redirect:/login";

        // 3. 권한 체크: 작성자 본인이거나 관리자(ADMIN)일 때만 수정 허용
        boolean isAuthor = (loginUser.getAppUserId() == originalComment.getAppUserId());
        boolean isAdmin = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAuthor || isAdmin) {
            reviewService.updateComment(dto);
            return "redirect:/reviews/list?recipeId=" + recipeId;
        } else {
            // 권한이 없으면 수정 안 하고 리스트로 튕겨내기 (메시지를 띄워주면 더 좋음)
            return "redirect:/reviews/list?recipeId=" + recipeId + "&error=no-permission";
        }
    }

    // 6. 답글 작성 페이지 이동
    @GetMapping("/reply")
    public String replyForm(@RequestParam int parentId, @RequestParam int reviewId, @RequestParam int recipeId, Model model) {
        model.addAttribute("parentId", parentId);
        model.addAttribute("reviewId", reviewId);
        model.addAttribute("recipeId", recipeId);
        return "reviews/replyComment";
    }

    // 7. 댓글/대댓글 저장
    @PostMapping("/addComment")
    public String addComment(CommentDto dto, @RequestParam int recipeId, Authentication authentication) {
        AppUserDto user = getLoginUser(authentication);
        if (user == null) return "redirect:/login";

        dto.setAppUserId(user.getAppUserId());
        if (dto.getParentId() == null) dto.setParentId(0);

        commentService.addComment(dto);
        return "redirect:/reviews/list?recipeId=" + recipeId;
    }

    // 8. 리뷰 삭제
    @PostMapping("/delete")
    public String deleteReview(@RequestParam int reviewId, @RequestParam int recipeId) {
        reviewService.deleteReview(reviewId);
        return "redirect:/reviews/list?recipeId=" + recipeId;
    }

    // 9. 댓글 삭제
    @PostMapping("/deleteComment")
    public String deleteComment(@RequestParam int commentId, @RequestParam int recipeId) {
        commentService.deleteComment(commentId);
        return "redirect:/reviews/list?recipeId=" + recipeId;
    }

    // 10. 리뷰 작성 폼
    @GetMapping("/comment")
    public String writeReviewForm(@RequestParam int recipeId, Model model) {
        model.addAttribute("recipeId", recipeId);
        return "reviews/comment";
    }
    // 11. 리뷰 등록하기 후 
    @PostMapping(value = "/comment", produces = "text/html; charset=UTF-8")
    @ResponseBody 
    public String writeReview(ReviewDto dto, Authentication authentication) {
        AppUserDto user = getLoginUser(authentication);
        
        // 1. 로그인 안 되어 있으면 알림 띄우고 로그인창으로
        if (user == null) {
            return "<script>alert('로그인이 필요한 서비스입니다.'); location.href='/login';</script>";
        }

        // 2. 리뷰 저장
        dto.setAppUserId(user.getAppUserId());
        reviewService.writeReview(dto);

        // 3. 알림 띄우고 다시 '상세 페이지'로 새로고침
        return "<script>" +
               "alert('리뷰가 성공적으로 등록되었습니다!');" + 
               "location.href = '/recipes/detail?recipeId=" + dto.getRecipeId() + "';" + 
               "</script>";
    }

    // 12. AI 요약 API
    @GetMapping("/api/summary")
    @ResponseBody
    public String getRealSummary(@RequestParam int recipeId,@RequestParam(defaultValue = "1") int page){
        List<ReviewDto> reviewList = reviewService.getReviewsByRecipeId(recipeId,page);
        if (reviewList == null || reviewList.isEmpty()) {
            return "아직 작성된 리뷰가 없어 요약할 수 없습니다.";
        }

        List<String> contents = new ArrayList<>();
        for (ReviewDto dto : reviewList) {
            contents.add(dto.getCommentText());
        }
        return aiApiService.summarizeReviews(contents);
    }

    // 13. AI 번역 API
    @GetMapping("/api/translate/smart")
    @ResponseBody
    public String smartTranslate(@RequestParam String text) {
        return aiApiService.translateSmart(text);
    }

    // 로그인 사용자 정보 가져오기
    public AppUserDto getLoginUser(Authentication authentication) {
        if (authentication == null) return null;

        String email = null;
        String provider = null;
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) principal;
            email = userDetails.getUser().getEmail();
            provider = userDetails.getUser().getProvider();
        } else if (principal instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) principal;
            email = (String) oAuth2User.getAttributes().get("email");
            if (authentication instanceof OAuth2AuthenticationToken) {
                provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
            }
        }

        return userService.selectEmail(email, provider);
    }
    
 // 추가: 관리자용 전체 리뷰 목록
    @GetMapping("/userlist")
    public String adminReviewList(Model model) {
        // 모든 리뷰를 가져오는 서비스 메서드가 있다고 가정 (없으면 만들어야 합니다)
        List<ReviewDto> allReviews = reviewService.getAllReviews(); 
        model.addAttribute("reviews", allReviews);
        
        return "reviews/userlist"; // 이동할 HTML 파일명 (아직 없으시면 만들어야 함)
    }
}
