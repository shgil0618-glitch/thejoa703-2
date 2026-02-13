package com.thejoa703.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagingDto {
	private int listtotal;      // #1) 전체글 수
	private int onepagelist;    // #2) 한 페이지당 보여줄 게시물 수 (10)
	private int pagetotal;      // #3) 총 페이지 수
	private int bottomlist;     // #4) 하단 페이지 버튼 개수 (10)
	private int pstartno;       // #5) (사용 안 함, current와 중복 가능성)
	
	private int current;        // #6) 현재 페이지 번호
	private int start;          // #7) 하단 페이지 시작 번호 (e.g., 1, 11, 21)
	private int end;            // #8) 하단 페이지 끝 번호 (e.g., 10, 20, 26)
	
	// ⭐️ [추가] DB 조회에 사용될 레코드 순번
	private int rStart;         // #9) DB 시작 레코드 번호 (rnum: 1, 11, 21...)
	private int rEnd;           // #10) DB 끝 레코드 번호 (rnum: 10, 20, 30...)
	
	// 생성자 수정: pstartno 대신 current를 매개변수로 사용하거나, 
    // DB에서 사용할 순번 계산 로직을 추가합니다.
    // 기존의 pstartno를 currentPage로 간주하고 수정합니다.
	public PagingDto(int listtotal, int currentPage) {
		super();
		this.listtotal = listtotal;
		this.onepagelist = 10;
		this.bottomlist = 10;
		this.current = currentPage;
        
        if (listtotal <= 0) { listtotal = 1; }
		this.pagetotal = (int) Math.ceil(listtotal / (double) onepagelist);
        
        // 1. DB 조회 순번 계산 (핵심)
        this.rEnd = current * onepagelist;
        this.rStart = rEnd - onepagelist + 1;
        
        // 2. 하단 페이지 번호 계산
		this.start = ((current - 1) / bottomlist) * bottomlist + 1; 
		this.end = start + bottomlist - 1;
        
		if (end > pagetotal) { end = pagetotal; }
        
        // (기존 PagingDto에서 사용하던 pstartno는 제거하거나,
        //  current와 같다고 보고 사용하지 않는 것을 권장합니다.)
	}
}