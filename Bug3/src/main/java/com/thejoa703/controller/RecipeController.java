package com.thejoa703.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.thejoa703.dto.AppUserDto;
import com.thejoa703.dto.Recipes3Dto;
import com.thejoa703.dto.RecipesStep3;
import com.thejoa703.dto.ReviewDto;
import com.thejoa703.dto.SearchDto;
import com.thejoa703.external.AiTextService;
import com.thejoa703.external.ApiModeration;
import com.thejoa703.security.CustomUserDetails;
import com.thejoa703.service.AppUserService;
import com.thejoa703.service.RecipeService;
import com.thejoa703.service.ReviewService;

@Controller
@RequestMapping("/recipes")
public class RecipeController {

	@Autowired
	private RecipeService recipeService;

	@Autowired
	private AppUserService userService;
	
	@Autowired 
	private ReviewService reviewService;

	@PreAuthorize("permitAll()")
	@GetMapping("/detail")
	public String detail(@RequestParam int recipeId,
						 @RequestParam(value = "page", defaultValue = "1") int page,
	                     Authentication authentication,
	                     Model model) {

	    AppUserDto loginUser = null;
	    Integer appUserId = null;

	    if (authentication != null) {
	        loginUser = getLoginUser(authentication);
	        if (loginUser != null) {
	            appUserId = loginUser.getAppUserId();
	        }
	    }

	    Recipes3Dto recipe = recipeService.getRecipeById(recipeId, appUserId);
	    if (recipe == null) {
	        return "redirect:/recipes/mylist";
	    }

	    model.addAttribute("recipe", recipe);
	    model.addAttribute("loginUser", loginUser); // ✅ 이거 중요
	    
	    // ⭐ [리뷰 페이징 로직 추가]
	    // 1. 현재 페이지의 리뷰 목록 가져오기 (수정된 서비스 호출)
	    List<ReviewDto> reviews = reviewService.getReviewsByRecipeId(recipeId, page);
	    
	    // 2. 전체 리뷰 개수 가져오기
	    int totalReviews = reviewService.getReviewCount(recipeId);
	    
	    // 3. 전체 페이지 수 계산 (한 페이지에 5개씩 기준)
	    int totalPages = (int) Math.ceil((double) totalReviews / 5);
	    
		/*
		 * // ⭐ [추가] 리뷰 목록 데이터를 가져와서 모델에 담기 // reviewService를 이용해 현재 레시피(recipeId)의 리뷰
		 * 리스트를 가져옵니다. List<ReviewDto> reviews =
		 * reviewService.getReviewsByRecipeId(recipeId);
		 */
	    
	    System.out.println("........" + reviews);
	    
	    model.addAttribute("reviews", reviews); // 👈 HTML에 'reviews'라는 이름으로 배달됨
	    model.addAttribute("currentPage", page);     // 현재 페이지 번호
	    model.addAttribute("totalPages", totalPages); // 전체 페이지 개수
	    return "recipes/detail";
	}


	// 📌 레시피 등록 폼
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/register")
	public String registerForm() {
		return "recipes/register";
	}

	// 📌 레시피 등록 처리 (레시피 + 재료 + 단계)
	@PostMapping("/register")
	@PreAuthorize("isAuthenticated()")
	public String register(Recipes3Dto dto,
			@RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
			@RequestParam(value = "stepImages", required = false) List<MultipartFile> stepImages,
			Authentication authentication, RedirectAttributes rttr) {

		AppUserDto user = getLoginUser(authentication);
		if (user == null) {
			rttr.addFlashAttribute("result", "로그인 후 이용 가능합니다.");
			return "redirect:/login";
		}

		dto.setAppUserId(user.getAppUserId());

		recipeService.createRecipe(imageFile, dto, stepImages);

		if ("private".equalsIgnoreCase(dto.getStatus())) {
			rttr.addFlashAttribute("result", "❗ 비속어가 검출되어 레시피가 비공개로 전환되었습니다.");
		} else {
			rttr.addFlashAttribute("result", "✅ 레시피 등록 성공");
		}

		return "redirect:/recipes/mylist";
	}

	@GetMapping("/modify")
	@PreAuthorize("isAuthenticated()")
	public String modifyForm(@RequestParam("recipeId") int recipeId, Authentication authentication, Model model,
			RedirectAttributes rttr) {

		Integer appUserId = null;
		AppUserDto user = getLoginUser(authentication);
		if (user != null)
			appUserId = user.getAppUserId();

		Recipes3Dto recipe = recipeService.getRecipeById(recipeId, appUserId);
		if (recipe == null) {
			rttr.addFlashAttribute("result", "레시피를 찾을 수 없습니다.");
			return "redirect:/recipes/mylist";
		}

		model.addAttribute("recipe", recipe);
		model.addAttribute("ingredients", recipeService.getIngredients(recipeId));
		model.addAttribute("steps", recipeService.getSteps(recipeId));

		return "recipes/modify";
	}

	// 📌 레시피 수정 처리 (레시피 + 재료 + 단계)
	@PostMapping("/modify")
	@PreAuthorize("isAuthenticated()")
	public String modify(@ModelAttribute Recipes3Dto dto, // DTO에 ingredients, steps 자동 바인딩
			@RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
			@RequestParam(value = "stepImages", required = false) List<MultipartFile> stepImages,
			RedirectAttributes rttr, Authentication authentication) {

		AppUserDto user = getLoginUser(authentication);
		if (user == null) {
			rttr.addFlashAttribute("result", "사용자 정보를 찾을 수 없습니다.");
			return "redirect:/users/login";
		}

		dto.setAppUserId(user.getAppUserId());

		int result = recipeService.updateRecipe(imageFile, dto, stepImages);

		// ✅ DB에서 다시 조회해서 최신 상태 확인
		Recipes3Dto updated = recipeService.getRecipeById(dto.getRecipeId(), user.getAppUserId());

		if ("private".equalsIgnoreCase(updated.getStatus())) {
			rttr.addFlashAttribute("result", "❗ 비속어가 검출되어 레시피가 비공개로 전환되었습니다.");
			return "redirect:/recipes/mylist";
		} else {
			rttr.addFlashAttribute("result", result > 0 ? "✅ 레시피 수정 성공" : "레시피 수정 실패");
			return "redirect:/recipes/detail?recipeId=" + dto.getRecipeId();
		}

	}

	@Autowired
	AiTextService aiTextService;

	// 📌 레시피 설명 자동 생성/수정 API
	@PostMapping("/auto-description")
	@PreAuthorize("isAuthenticated()")
	@ResponseBody
	public ResponseEntity<?> autoDescription(@RequestBody Map<String, Object> payload, Authentication authentication) {
		AppUserDto user = getLoginUser(authentication);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 필요");
		}

		String title = (String) payload.get("title");
		List<String> ingredients = (List<String>) payload.get("ingredients");
		List<String> steps = (List<String>) payload.get("steps");

		// AI 호출해서 설명 생성
		String description = aiTextService.generateDescription(title, ingredients, steps);

		Map<String, Object> result = new HashMap<>();
		result.put("description", description);
		return ResponseEntity.ok(result);
	}

	// 📌 레시피 단계 자동 생성 API
	@PostMapping("/auto-steps")
	@PreAuthorize("isAuthenticated()")
	@ResponseBody
	public ResponseEntity<?> autoSteps(@RequestBody Map<String, Object> payload, Authentication authentication) {
		AppUserDto user = getLoginUser(authentication);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 필요");
		}

		String title = (String) payload.get("title");
		String shortDesc = (String) payload.get("shortDesc"); // 간단 설명
		List<String> ingredients = (List<String>) payload.get("ingredients");

		// AI 호출해서 단계 생성
		String stepsText = aiTextService.generateSteps(title, shortDesc, ingredients);

		Map<String, Object> result = new HashMap<>();
		result.put("steps", stepsText);
		return ResponseEntity.ok(result);
	}

	// 📌 레시피 삭제 폼
    @GetMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    public String deleteForm(@RequestParam("recipeId") int recipeId,
                             Authentication authentication,
                             Model model,
                             RedirectAttributes rttr) {

        AppUserDto user = getLoginUser(authentication);
        if (user == null) {
            rttr.addFlashAttribute("result", "로그인 후 이용 가능합니다.");
            return "redirect:/login";
        }

        // ✅ 관리자 여부 체크 (isAdmin 메서드 없이)
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        // 관리자면 userId 무시하고 조회
        Recipes3Dto recipe = isAdmin
                ? recipeService.getRecipeById(recipeId, null)
                : recipeService.getRecipeById(recipeId, user.getAppUserId());

        if (recipe == null) {
            rttr.addFlashAttribute("result", "삭제할 레시피를 찾을 수 없습니다.");
            return "redirect:/recipes/mylist";
        }

        // ❗ 관리자 아닐 때만 작성자 체크
        if (!isAdmin && !recipe.getAppUserId().equals(user.getAppUserId())) {
            rttr.addFlashAttribute("result", "본인이 작성한 레시피만 삭제 가능합니다.");
            return "redirect:/recipes/detail?recipeId=" + recipeId;
        }

        model.addAttribute("dto", recipe);
        return "recipes/delete";
    }


 // 📌 레시피 삭제 처리
    @PostMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    public String deleteRecipe(@RequestParam("recipeId") int recipeId,
                               Authentication authentication,
                               RedirectAttributes rttr) {

        String resultMessage = "레시피 삭제 실패";
        String redirectUrl = "redirect:/recipes/mylist"; // 기본값: 일반 유저

        try {
            AppUserDto user = getLoginUser(authentication);
            if (user == null) {
                rttr.addFlashAttribute("result", "로그인 후 이용 가능합니다.");
                return "redirect:/login";
            }

            // ✅ 관리자 여부 체크
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            // ✅ 관리자면 list로 이동
            if (isAdmin) {
                redirectUrl = "redirect:/recipes/list";
            }

            Recipes3Dto recipe = isAdmin
                    ? recipeService.getRecipeById(recipeId, null)
                    : recipeService.getRecipeById(recipeId, user.getAppUserId());

            if (recipe == null) {
                rttr.addFlashAttribute("result", "삭제할 레시피를 찾을 수 없습니다.");
                return redirectUrl;
            }

            // ❗ 관리자 아닐 때만 작성자 체크
            if (!isAdmin && !recipe.getAppUserId().equals(user.getAppUserId())) {
                rttr.addFlashAttribute("result", "본인이 작성한 레시피만 삭제 가능합니다.");
                return "redirect:/recipes/detail?recipeId=" + recipeId;
            }

            int result = recipeService.deleteRecipe(recipeId);
            if (result > 0) {
                resultMessage = "레시피가 정상적으로 삭제되었습니다.";
            }

        } catch (Exception e) {
            e.printStackTrace();
            resultMessage = "삭제 중 오류가 발생했습니다.";
        }

        rttr.addFlashAttribute("result", resultMessage);
        return redirectUrl;
    }

	

	// 📌 레시피 목록 (페이징)
	// 리스트 + 검색
	/* @PreAuthorize("permitAll()") */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping("/list")
	public String list(@RequestParam(value = "page", defaultValue = "1") int currentPage,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "searchField", defaultValue = "ALL") String searchField,
			org.springframework.security.core.Authentication authentication, Model model) {
		AppUserDto user = getLoginUser(authentication);
		Integer appUserId = (user != null) ? user.getAppUserId() : -1;
		Map<String, Object> params = new HashMap<>();
		params.put("currentPage", currentPage);
		params.put("keyword", keyword);
		params.put("searchField", searchField);
		params.put("appUserId", appUserId);
		// 검색 필드 구성
		if ("ALL".equalsIgnoreCase(searchField)) {
			params.put("fields", Arrays.asList("TITLE", "AUTHOR"));
		} else {
			params.put("fields", Collections.singletonList(searchField));
		}
		Map<String, Object> result = recipeService.listSearchRecipes(params);
		model.addAttribute("list", result.get("list"));
		model.addAttribute("paging", result.get("paging"));
		model.addAttribute("keyword", keyword);
		model.addAttribute("searchField", searchField);
		return "recipes/list";
	}

	/*
	 * @PreAuthorize("permitAll()")
	 * 
	 * @GetMapping("/list") public String list(@RequestParam(value = "page",
	 * defaultValue = "1") int currentPage,
	 * 
	 * @RequestParam(value = "category", required = false) Integer category,
	 * Authentication authentication, Model model) {
	 * 
	 * int totalCount = recipeService.countAll(category); PagingDto paging = new
	 * PagingDto(totalCount, currentPage); model.addAttribute("paging", paging);
	 * 
	 * Map<String,Object> params = new HashMap<>(); params.put("rStart",
	 * paging.getRStart()); params.put("rEnd", paging.getREnd());
	 * params.put("category", category);
	 * 
	 * // 로그인 사용자 ID 추가 Integer appUserId = null; AppUserDto user =
	 * getLoginUser(authentication); if (user != null) appUserId =
	 * user.getAppUserId(); params.put("appUserId", appUserId != null ? appUserId :
	 * -1);
	 * 
	 * List<Recipes3Dto> recipeList = recipeService.selectRecipeAllPaged(params);
	 * model.addAttribute("list", recipeList);
	 * 
	 * return "recipes/list"; }
	 */
	@PreAuthorize("permitAll()")
	@GetMapping("/search")
	public Object search(
	        @RequestParam(value = "page", defaultValue = "1") int currentPage,
	        @RequestParam(value = "keyword", required = false) String keyword,
	        @RequestParam(value = "searchField", defaultValue = "ALL") String searchField,
	        @RequestParam(value = "sort", defaultValue = "LATEST") String sort,
	        @RequestParam(value = "category", required = false) Integer category,
	        @RequestParam(value = "ajax", defaultValue = "false") boolean ajax,
	        @RequestParam(value = "fields", required = false) List<String> fields,
	        Authentication authentication,
	        Model model) {

	    SearchDto searchDto = new SearchDto();
	    searchDto.setKeyword(keyword);
	    searchDto.setSearchField(searchField);
	    searchDto.setSort(sort);
	    searchDto.setCategory(category);
	    searchDto.setCurrentPage(currentPage);

	    if (fields != null && !fields.isEmpty()) {
	        searchDto.setFields(fields);
	    } else if ("ALL".equals(searchField)) {
	        searchDto.setFields(Arrays.asList("TITLE", "AUTHOR", "DESCRIPTION"));
	    } else {
	        searchDto.setFields(Collections.singletonList(searchField));
	    }

	    // ✅ 로그인 사용자 ID 처리 (정답)
	    Integer appUserId = 0;
	    AppUserDto loginUser = getLoginUser(authentication);
	    if (loginUser != null) {
	        appUserId = loginUser.getAppUserId();
	    }
	    searchDto.setAppUserId(appUserId);

	    Map<String, Object> result = recipeService.searchRecipes(searchDto);

	    if (ajax) {
	        return ResponseEntity.ok(result);
	    }

	    model.addAttribute("list", result.get("list"));
	    model.addAttribute("paging", result.get("paging"));
	    model.addAttribute("keyword", keyword);
	    model.addAttribute("searchField", searchField);
	    model.addAttribute("sort", sort);
	    model.addAttribute("category", category);
	    model.addAttribute("suggestion", result.get("suggestion"));
	    return "recipes/search";
	}




	@PreAuthorize("isAuthenticated()")
	@GetMapping("/mylist")
	public String myList(Authentication authentication, Model model, RedirectAttributes rttr) {
		if (authentication == null) {
			rttr.addFlashAttribute("result", "로그인 후 이용 가능합니다.");
			return "redirect:/login";
		}

		String email = null, provider = null;
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

		var user = userService.selectEmail(email, provider);
		if (user == null) {
			rttr.addFlashAttribute("result", "사용자 정보를 찾을 수 없습니다.");
			return "redirect:/users/login";
		}

		List<Recipes3Dto> myList = recipeService.selectMyRecipes(user.getAppUserId());
		model.addAttribute("list", myList);

		return "recipes/mylist";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/likes")
	public String likedRecipes(Authentication authentication, Model model, RedirectAttributes rttr) {
		if (authentication == null) {
			rttr.addFlashAttribute("result", "로그인 후 이용 가능합니다.");
			return "redirect:/login";
		}

		String email = null, provider = null;
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

		var user = userService.selectEmail(email, provider);
		if (user == null) {
			rttr.addFlashAttribute("result", "사용자 정보를 찾을 수 없습니다.");
			return "redirect:/users/login";
		}

		List<Recipes3Dto> likedList = recipeService.selectLikedRecipes(user.getAppUserId());
		model.addAttribute("list", likedList);

		return "recipes/likes";
	}

	// 📌 좋아요 추가
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/like")
	@ResponseBody
	public Map<String, Object> likeRecipe(@RequestParam("recipeId") int recipeId, Authentication authentication) {
		Map<String, Object> result = new HashMap<>();
		try {
			AppUserDto user = getLoginUser(authentication);
			if (user == null) {
				result.put("success", false);
				result.put("error", "로그인 필요");
				return result;
			}

			recipeService.likeRecipe(user.getAppUserId(), recipeId);

			result.put("success", true);
			result.put("likes", recipeService.countLikesByRecipe(recipeId));
		} catch (Exception e) {
			result.put("success", false);
			result.put("error", e.getMessage());
		}
		return result;
	}

	// 📌 좋아요 취소
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/unlike")
	@ResponseBody
	public Map<String, Object> unlikeRecipe(@RequestParam("recipeId") int recipeId, Authentication authentication) {
		Map<String, Object> result = new HashMap<>();
		try {
			AppUserDto user = getLoginUser(authentication);
			if (user == null) {
				result.put("success", false);
				result.put("error", "로그인 필요");
				return result;
			}

			recipeService.unlikeRecipe(user.getAppUserId(), recipeId);

			result.put("success", true);
			result.put("likes", recipeService.countLikesByRecipe(recipeId));
		} catch (Exception e) {
			result.put("success", false);
			result.put("error", e.getMessage());
		}
		return result;
	}

	// 📌 좋아요 개수 조회
	@GetMapping("/likes/count")
	@ResponseBody
	public Map<String, Object> countLikes(@RequestParam("recipeId") int recipeId) {
		Map<String, Object> result = new HashMap<>();
		result.put("likes", recipeService.countLikesByRecipe(recipeId));
		return result;
	}

	public AppUserDto getLoginUser(Authentication authentication) {
		if (authentication == null)
			return null;

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

	// ✅ 비속어 전체 조회 (로그인 사용자만 가능)
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/filter-badwords")
	public String filterBadWordsPage(Model model) {
		// 전체 레시피 목록을 가져와서 모델에 담음
		List<Recipes3Dto> recipes = recipeService.selectRecipeAllPaged(Map.of("rStart", 1, "rEnd", 100));
		model.addAttribute("recipes", recipes);
		return "recipes/filterBadWords"; // => templates/recipes/filterBadWords.html
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/badwords")
	public String badWordsPage(@RequestParam(value = "page", defaultValue = "1") int currentPage, Model model) {
		Map<String, Object> result = recipeService.getBadWordsPaged(currentPage);

		model.addAttribute("badwords", result.get("list"));
		model.addAttribute("paging", result.get("paging"));

		return "recipes/badWords"; // => templates/recipes/badWords.html
	}

	// ✅ 비속어 등록
	/* @PreAuthorize("isAuthenticated()") */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping("/badwords")
	public String addBadWord(@RequestParam String word, RedirectAttributes rttr) {
		try {
			recipeService.addBadWord(word);
			rttr.addFlashAttribute("success", true);
			rttr.addFlashAttribute("message", "비속어 등록 완료: " + word);
		} catch (Exception e) {
			rttr.addFlashAttribute("success", false);
			rttr.addFlashAttribute("error", "등록 실패: " + e.getMessage());
		}
		return "redirect:/recipes/badwords"; // 경로에 admin 없음
	}

	// ✅ 비속어 삭제
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/badwords/delete")
	public String deleteBadWord(@RequestParam int wordId, RedirectAttributes rttr) {
		try {
			recipeService.deleteBadWordById(wordId);
			rttr.addFlashAttribute("success", true);
			rttr.addFlashAttribute("message", "비속어 삭제 완료 (ID=" + wordId + ")");
		} catch (Exception e) {
			rttr.addFlashAttribute("success", false);
			rttr.addFlashAttribute("error", "삭제 실패: " + e.getMessage());
		}
		return "redirect:/recipes/badwords";
	}

	@Autowired
	ApiModeration apiModeration;

	@PostMapping("/moderation-check/{recipeId}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public String moderationCheckOne(@PathVariable int recipeId, RedirectAttributes rttr,
			Authentication authentication) {

		Recipes3Dto recipe = recipeService.getRecipeById(recipeId, null);
		if (recipe == null) {
			rttr.addFlashAttribute("result", "레시피를 찾을 수 없습니다.");
			return "redirect:/recipes/list";
		}

		boolean flagged = false;
		// 제목 / 설명 / 단계별 설명 검사
		if (apiModeration.detectBadWords(recipe.getTitle()) || apiModeration.detectBadWords(recipe.getDescription())) {
			flagged = true;
		}
		if (recipe.getSteps() != null) {
			for (RecipesStep3 step : recipe.getSteps()) {
				if (apiModeration.detectBadWords(step.getStepDesc())) {
					flagged = true;
					break;
				}
			}
		}

		if (flagged) {
//			recipe.setStatus("PRIVATE");
//			recipeService.updateRecipe(null, recipe, null);
//			rttr.addFlashAttribute("result", "❗ 비속어가 검출되어 레시피가 비공개로 전환되었습니다.");
			 recipeService.changeRecipeStatus(recipeId, "PRIVATE");
		        rttr.addFlashAttribute("result",
		                "❗ 비속어가 검출되어 레시피가 비공개로 전환되었습니다.");
		} else {
			rttr.addFlashAttribute("result", "✅ 비속어가 검출되지 않았습니다.");
		}

		return "redirect:/recipes/list";
	}

	// 검색 기록 저장
	@PostMapping("/save")
	public ResponseEntity<String> saveSearch(@RequestParam Integer appUserId, @RequestParam String keyword) {
		recipeService.saveSearchHistory(appUserId, keyword);
		return ResponseEntity.ok("검색어 저장 완료");
	}

// 인기 키워드 조회
	@GetMapping("/top")
	public List<Map<String, Object>> getTopKeywords(@RequestParam(defaultValue = "10") int limit) {
		return recipeService.topKeywords(limit);
	}
	
	
	// 랜덤 추천
	// RecipeController.java
	@GetMapping("/recommend")
	@ResponseBody 
	public ResponseEntity<?> recommendRecipe(
	        @RequestParam(value="category", required=false) Integer category,
	        @RequestParam(value="keyword", required=false) String keyword, 
	        @RequestParam(value="highQuality", defaultValue="false") String highQuality) {
	    
	    // 서비스 호출 (카테고리/키워드 중 하나만 있어도 작동)
	    Recipes3Dto randomRecipe = recipeService.getRandomRecipe(category, keyword, highQuality);
	    
	    if (randomRecipe == null) {
	        // 조건에 맞는 데이터가 없을 때 404가 아닌 204(No Content)나 에러 메시지 반환
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("조건에 맞는 레시피가 없습니다.");
	    }
	    
	    return ResponseEntity.ok(randomRecipe);
	}
	

}
