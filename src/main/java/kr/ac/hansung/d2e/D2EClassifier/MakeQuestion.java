package kr.ac.hansung.d2e.D2EClassifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import linguaFile.FileIO;
import rhino.RHINO;

public class MakeQuestion {
	FileIO fio = new FileIO();


	/**
	 * 주어진 범위에서 중복되지 않는 수를 갖는 배열을 만듭니다.
	 * @param min 범위 내 가장 작은 수
	 * @param max 범위 내 가장 큰 수
	 * @param random 수를 랜덤으로 섞어서 넣을지 여부
	 * @return
	 */
	public int[] makeRandomArr(int min, int max, boolean random) {
		ArrayList<Integer> list = new ArrayList<>(max+1);
		for (int i = min; i <= max; i++){
			list.add(i);
		}
		int[] arr = new int[max];
		if (random)	{				//랜덤으로 수를 넣는 경우
			for (int count = 0; count < max; count++){
				arr[count] = list.remove((int)(Math.random() * list.size()));
			}
		}
		else {							//순서대로 수를 넣는 경우
			for (int count = 0; count < max; count++){
				arr[count] = list.get(count);
			}
		}
		return arr;
	}


	/**
	 * 질문과 선택지를 출력한다
	 * @param expression 분석된 사전 내용
	 * @param questNum 선택된 질문의 번호
	 * @param questCol 질문이 있는 컬럼의 번호
	 * @return
	 */
	public String[] makeQuestionNSheets(String[][] expression, int questNum, int questCol, boolean print)
	{
		int selectColAmount = (expression[questNum].length -1) - questCol;
		String quest = expression[questNum][questCol];							// 질문 내용

		String[] sheets = new String[selectColAmount+3];						// 선택지를 받는 배열. 번호를 맞추기 위해 처음을 비워두고, 마지막에 정답번호와 정답을 담는다
		int start = questCol+1;
		int end = questCol+selectColAmount;

		for (int i=start, j=1; i<=end; i++, j++) {										// 선택지의 내용을 하나씩 배열에 담는다
			sheets[j] = expression[questNum][i].trim();
		}

		if(print)
			System.out.println(quest);														// 질문 출력
		for (int i=1; i<selectColAmount+1; i++) {									// 선택지 출력
			String str = i + "번, " + sheets[i];
			if(print)
				System.out.println(str);
		}

		sheets[selectColAmount+1] = expression[questNum][0].trim();		// 정답
		sheets[selectColAmount+2] = expression[questNum][1].trim();		// 정답번호

		return sheets;
	}



	/**
	 * 정답을 입력했는지 등을 판단합니다.
	 * @param sheets
	 * @param input
	 * @param rn
	 * @return
	 */
	public String judgeSelection(String[] sheets, String input, RHINO rn)
	{
		String found = "NULL";

		String[] cklist = {"NNG", "NNP", "NR", "SN", "MM"};							//명사류와 숫자만 준비해둔다. "첫, 두, 세, 네"는 MM이다.
		String[] inputArr = rn.GetOutputPartArr_cklist(rn.ExternCall(input, true), cklist);

		// 정답의 양식과 동일하게 입력한 경우인지를 검색한다 (예: 오타와 / 1 / 1번)
		if(found.equals("NULL"))
			found = this.judgeSelection_Same(sheets, input);

		// 복잡한 입력문으로서, 형태소 분석 후 나온 하나의 결과가 정답의 양식과 동일한지 검색한다 (예: 오타와요 / 1이요 / 1번이요 / 일번이요)
		if(found.equals("NULL"))
			found = this.judgeSelection_MorphSame(sheets, inputArr);

		// 자카드 유사도로 처리
		if(found.equals("NULL"))
			found = this.judgeSelection_Jaccard(sheets, inputArr);

		return found;
	}

	/**
	 * 입력문이 정답 또는 선택지와 동일하게(equals) 작성되었는지를 확인합니다
	 * '오타와'처럼 문자로 일치하거나, '1' 또는 '1번'으로 숫자로 일치하는 경우를 찾습니다.
	 * @param sheets
	 * @param input
	 * @return
	 */
	private String judgeSelection_Same(String[] sheets, String input)
	{
		String found = "NULL";
		input= changeStringToNum(input);						// 문자형 숫자를 숫자형 숫자로 바꿔준다

		if(input.length()==2 && input.endsWith("번"))			// "1번 --> 1" 로 수정한다.
			input = input.replace("번", "");

		if(sheets[sheets.length-2].equals(input)) {				// 문자열로 검색 (예: 워싱턴)
			found = "RIGHT";
		}
		else if(sheets[sheets.length-1].equals(input))	{		// 번호로 검색 (예: 2)
			found = "RIGHT";
		}

		if(found.equals("NULL"))										// 만약 아직 발견되지 않았다면
		{
			for(int i=1; i<sheets.length-2; i++) {
				if(sheets[i].equals(input)) {							// 입력문이 1~4의 선택지에 있다면
					found = "WRONG";
					break;
				}
			}
		}

		if(found.equals("NULL"))										// 만약 아직 발견되지 않았다면
		{
			for(int i=1; i<sheets.length-2; i++) {
				if(Integer.toString(i).equals(input)) {				// 그 번호가 1~4 사이에 있다면
					found = "WRONG";
					break;
				}
			}
		}

		return found;
	}


	/**
	 * 문자형 숫자를 숫자형 숫자로 바꿔준다
	 * 완전 일치 검색
	 * @param input
	 * @return
	 */
	private String changeStringToNum(String input)
	{
		if(input.equals("일")||input.equals("일번")||input.equals("하나")||input.equals("첫")||input.equals("처음")||input.equals("첫째"))
			input = "1";
		else if(input.equals("이")||input.equals("이번")||input.equals("둘")||input.equals("두")||input.equals("둘째")||input.equals("두째"))
			input = "2";
		else if(input.equals("삼")||input.equals("삼번")||input.equals("셋")||input.equals("세")||input.equals("셋째")||input.equals("세째"))
			input = "3";
		else if(input.equals("사")||input.equals("사번")||input.equals("넷")||input.equals("네")||input.equals("넷째")||input.equals("네째"))
			input = "4";
		else if(input.equals("오")||input.equals("오번")||input.equals("다섯")||input.equals("다섯째"))
			input = "5";
		else if(input.equals("육")||input.equals("육번")||input.equals("여섯")||input.equals("여섯째"))
			input = "6";
		else if(input.equals("칠")||input.equals("칠번")||input.equals("일곱")||input.equals("일곱째"))
			input = "7";
		else if(input.equals("팔")||input.equals("팔번")||input.equals("여덟")||input.equals("여덟째"))
			input = "8";
		else if(input.equals("구")||input.equals("구번")||input.equals("아홉")||input.equals("아홉째"))
			input = "9";
		else if(input.equals("십")||input.equals("십번")||input.equals("열")||input.equals("열째"))
			input = "10";

		return input;
	}


	/**
	 * 복잡한 표현 속에 있어 형태소 분석 결과로 진행하는 경우입니다.
	 * 그러나 형태소가 단 하나만 있는 경우를 대상으로 합니다.
	 * 형태소가 둘 이상 있으면 자카드 유사도로 처리해야 합니다.
	 * @param sheets
	 * @param inputArr
	 * @return
	 */
	private String judgeSelection_MorphSame(String[] sheets, String[] inputArr)
	{
		String found = "NULL";

		if(inputArr.length==1)														//해당 요소가 하나인 경우에 대해서만 진행한다
			found = this.judgeSelection_Same(sheets, inputArr[0]);

		return found;
	}


	/**
	 * 명사류나 숫자가 둘 이상 있어서 자카드 유사도 방법으로 처리하는 경우입니다.
	 * @param sheets
	 * @param inputArr
	 * @return
	 */
	private String judgeSelection_Jaccard(String[] sheets, String[] inputArr)
	{
		String found = "NULL";
		int rightCnt = 0;
		int wrongCnt = 0;

		for(int i=0; i<inputArr.length; i++)										//해당 요소가 둘 이상인 경우에 대해서 진행한다
		{
			found = this.judgeSelection_Same(sheets, inputArr[i]);

			if(found.equals("RIGHT"))
				rightCnt++;
			else if(found.equals("WRONG"))
				wrongCnt++;
		}

		if(rightCnt > wrongCnt)
		{
			found = "RIGHT";
		}
		else if(rightCnt < wrongCnt)
		{
			found = "WRONG";
		}
		else if(rightCnt==0 && wrongCnt==0)
		{
			found = "UNKNOWN";
		}
		else if(rightCnt == wrongCnt)
		{
			found = "SAME";
		}

		return found;
	}


	/**
	 * 정답이 없이 사용자의 입력문이 선택지의 어떤 것과 가장 많이 일치하는지를 본다
	 * @param sheets
	 * @param input
	 * @param rn
	 * @return
	 */
	public String[] judgeSelection2(String[] sheets, String input, RHINO rn, String[] cklist)
	{
		String[] result = {"NULL", "NULL", "NULL"};
		String[] inputArr = rn.GetOutputPartArr_cklist(rn.ExternCall(input, true), cklist);

		// 정답의 양식과 동일하게 입력한 경우인지를 검색한다 (예: 오타와 / 1 / 1번)
		//if(found.equals("NULL"))
		//	result = this.judgeSelection_Same2_outer(sheets, input);

		// 약간 복잡한 입력문으로서, 하나의 결과만 있으며, 형태소 분석 후 나온 하나의 결과가 정답의 양식과 동일한지 검색한다 (예: 오타와요 / 1이요 / 1번이요 / 일번이요)
		//if(result[0].equals("NULL"))
		//	result = this.judgeSelection_MorphSame2(sheets, inputArr);

		// 아주 복잡한 입력문으로서, 여러개의 결과가 있으며, 자카드 유사도로 처리. (예: 정답은 오타와 같아요 / 정답은 오타와 같기도 하고 밴쿠버 같기도 한데, 1번으로 할래)
		// 결국 이 함수 하나로 모든 경우를 처리할 수 있다
		if(result[0].equals("NULL"))
			result = this.judgeSelection_Jaccard2(sheets, inputArr);

		return result;
	}


	@SuppressWarnings("unused")
	private String[] judgeSelection_Same2_outer(String[] sheets, String input)
	{
		String[] result = this.judgeSelection_Same2_inner(sheets, input);

		return result;
	}


	/**
	 * 입력문이 선택지와 동일하게(equals) 작성되었는지를 확인합니다
	 * '오타와'처럼 문자로 일치하거나, '1' 또는 '1번'으로 숫자로 일치하는 경우를 찾습니다.
	 * 입력문과 선택지가 동일하기만 하면 무조건 맞는 것으로 판단합니다.
	 * @param sheets
	 * @param input
	 * @return
	 */
	private String[] judgeSelection_Same2_inner(String[] sheets, String input)
	{
		String[] result = {"NULL", "NULL", "NULL"};
		String found = "NULL";
		String select = "NULL";
		input= this.changeStringToNum(input);				// 문자형 숫자를 숫자형 숫자로 바꿔준다

		if(input.length()==2 && input.endsWith("번"))		// "1번 --> 1" 로 수정한다.
			input = input.replace("번", "");

		if(found.equals("NULL"))									// 만약 아직 발견되지 않았다면
		{
			for(int i=1; i<sheets.length-2; i++) {
				if(sheets[i].equals(input)) {							// 입력문이 선택지에 있다면 무조건 맞음
					found = "RIGHT";
					select = String.valueOf(i-1);
					break;
				}
			}
		}

		if(found.equals("NULL"))									// 만약 아직 발견되지 않았다면
		{
			for(int i=1; i<sheets.length-2; i++) {
				if(Integer.toString(i).equals(input)) {				// 그 번호가 선택지 범위에 있다면 무조건 맞음
					found = "RIGHT";
					select = String.valueOf(i-1);
					break;
				}
			}
		}

		result[0] = found;
		result[1] = this.findIndexOfWord(sheets, select);
		result[2] = select;

		return result;
	}


	/**
	 * 사용자의 답변 목록 중 기수 숫자(1, 2, 3, 4)로 되어 있는 것을 목록의 문자로 변환합니다.
	 * @param sheets
	 * @param inputArr
	 * @return
	 */
	private String[] intToStrAnswer(String[] sheets, String[] inputArr)
	{
		String regExp = "^[0-9]+$";								//숫자 인식을 위한 정규식

		for(int i=0; i<inputArr.length; i++)
		{
			if(inputArr[i].matches(regExp))
			{
				int strNum = Integer.valueOf(inputArr[i]);
				if(strNum <= sheets.length-3)					// 그 숫자가 선택지보다 큰 것은 반영하지 않는다
					inputArr[i] = sheets[strNum];					// 선택지의 해당 번호로 대체한다
			}
		}

		return inputArr;
	}


	/**
	 * 복잡한 표현 속에 있어 형태소 분석 결과로 진행하는 경우입니다.
	 * 그러나 형태소가 단 하나만 있는 경우를 대상으로 합니다.
	 * 형태소가 둘 이상 있으면 자카드 유사도로 처리해야 합니다.
	 * @param sheets
	 * @param inputArr
	 * @return
	 */
	@SuppressWarnings("unused")
	private String[] judgeSelection_MorphSame2(String[] sheets, String[] inputArr)
	{
		String[] result = {"NULL", "NULL", "NULL"};

		if(inputArr.length==1)														//해당 요소가 하나인 경우에 대해서만 진행한다
			result = this.judgeSelection_Same2_inner(sheets, inputArr[0]);

		return result;
	}


	/**
	 * 사용자의 답변 목록 중 서수 숫자(첫, 두, 세, 네)로 되어 있는 것을 목록의 문자로 변환합니다.
	 * @param sheets
	 * @param inputArr
	 * @return
	 */
	private String[] ordinalToCardinal(String[] sheets, String[] inputArr)
	{
		boolean found = false;
		String regExp = "^[0-9]+$";								//숫자 인식을 위한 정규식
		String strNumCandidate = "";

		//{"method":"choice", "input":{"choice": ["둘째 아들","첫째 아들","셋째 아들","답이 없다"], "answer": "둘째 아들이요"}}  를 위한 부분
		for(int i=0; i<inputArr.length-1; i++)
		{
			for(int j=1; j<sheets.length-2; j++)
			{
				if(sheets[j].startsWith(inputArr[i]))
				{
					if(sheets[j].contains(inputArr[i+1]))
					{
						inputArr[i] = sheets[j];					// 선택지의 해당 번호로 대체한다
						found = true;
						break;
					}
				}
			}
			if(found)
				break;
		}

		if(!found)
		{
			for(int i=0; i<inputArr.length; i++)
			{
				strNumCandidate = changeStringToNum(inputArr[i]);							//일치 검색으로 해결
				if(strNumCandidate.matches(regExp))
				{
					int strNum = Integer.valueOf(strNumCandidate);
					if(strNum <= sheets.length-3)					// 그 숫자가 선택지보다 큰 것은 반영하지 않는다
						inputArr[i] = sheets[strNum];					// 선택지의 해당 번호로 대체한다
				}
			}
		}

		return inputArr;
	}


	/**
	 * 새로 찾은 정답 단어를 등록한다
	 * @param sheets
	 * @param inputArr
	 * @param rightWord
	 * @return
	 */
	private String[][] enrollRightWord(String[] sheets, String[] inputArr, String[][] rightWord)
	{
		String[] result_temp = new String[2];

		for(int i=0; i<rightWord.length; i++)
		{
			rightWord[i][0] = "null";
			rightWord[i][1] = "null";
		}

		for(int i=0; i<inputArr.length; i++)
		{
			result_temp = this.judgeSelection_Same2_inner(sheets, inputArr[i]);
			boolean isthere = false;

			for(int j=0; j<rightWord.length; j++)	{
				if(rightWord[j][0].equals(inputArr[i]))		{				//이미 있는 경우, 카운트 올림
					isthere = true;
					int temp = Integer.valueOf(rightWord[j][1]);
					rightWord[j][1] = String.valueOf(temp+1);
					break;
				}
			}

			if(!isthere)	{														//아직 없는 경우 새로 등록
				if(result_temp[0].equals("RIGHT"))	{
					for(int j=0; j<rightWord.length; j++)	{
						if(rightWord[j][0]=="null")	{
							rightWord[j][0] = inputArr[i];
							rightWord[j][1] = "1";
							break;
						}
					}
				}
			}
		}

		return rightWord;
	}


	/**
	 * 새로 찾은 정답 단어를 숫자형으로 변환한다
	 * @param rightWord
	 * @param rightWord2
	 * @return
	 */
	private int[][] enrollRightWord2(String[][] rightWord, int[][] rightWord2)
	{
		//int 배열로 Copy
		for(int i=0; i<rightWord.length; i++)
		{
			if(rightWord[i][0].equals("null")) {
				break;
			}
			else {
				int temp = Integer.valueOf(rightWord[i][1]);
				rightWord2[i][0] = i;
				rightWord2[i][1] = temp;
			}
		}

		//내림차순 정렬
		Arrays.sort(rightWord2, new Comparator<int[]>() {
			@Override
			public int compare(int[] o1, int[] o2) {
				return Integer.compare(o2[1], o1[1]);
			}
		});

		return rightWord2;
	}


	/**
	 * null을 제외한 실질적인 배열의 길이를 찾는다
	 * @param rightWord
	 * @param rightWord2
	 * @return
	 */
	private int findArrayInt(String[][] rightWord, int[][] rightWord2)
	{
		int arrayInt = 0;
		for(int i=0; i<rightWord.length; i++)
		{
			if(rightWord[i][0].equals("null")) {
				arrayInt = i;											//0 원소 제거를 위해서는 불필요하게 증가된 1을 다시 낮춰야 한다
				break;
			}
		}

		if(rightWord2.length==1 && arrayInt==0)		//특수 예외
			arrayInt = 1;

		return arrayInt;
	}


	/**
	 * 결과 배열 result를 만듭니다.
	 * @param sheets
	 * @param rightWord
	 * @param rightWord2
	 * @return
	 */
	private String[] makeResult(String[] sheets, String[][] rightWord, int[][] rightWord2)
	{
		String[] result = {"NULL", "NULL", "NULL"};
		int arrayInt = findArrayInt(rightWord, rightWord2);					//0 원소를 제거하기
		int[][] rightWord3 = new int[arrayInt][2];

		for(int i=0; i<arrayInt; i++)
		{
			rightWord3[i][0] = rightWord2[i][0];
			rightWord3[i][1] = rightWord2[i][1];
		}

		if(rightWord3.length==0) {
			result[0] = "UNKNOWN";
			result[1] = "NULL";
			result[2] = "NULL";
		}
		else if(rightWord3.length==1) {
			result[0] = "RIGHT";
			result[1] = rightWord[0][0];
			result[2] = this.findIndexOfWord(sheets, result[1]);
		}
		else if(rightWord3.length >= 2)	{
			if(rightWord3[0][1]==rightWord3[1][1]) {			//최소한 두 개가 동등한 빈도를 갖는다면 그것은 혼란스러운 상태이다
				result[0] = "SAME";
				result[1] = "NULL";
				result[2] = "NULL";
			}
			else {
				int selectIdx = rightWord3[0][0];					//가장 빈도가 높은 어휘의 인덱스
				String select = rightWord[selectIdx][0];
				result[0] = "RIGHT";
				result[1] = select;
				result[2] = this.findIndexOfWord(sheets, select);
			}
		}
		else {
			int selectIdx = rightWord3[0][0];						//가장 빈도가 높은 어휘의 인덱스
			String select = rightWord[selectIdx][0];
			result[0] = "RIGHT";
			result[1] = select;
			result[2] = this.findIndexOfWord(sheets, select);
		}

		return result;
	}


	/**
	 * 명사류나 숫자가 둘 이상 있어서 자카드 유사도 방법으로 처리하는 경우입니다.
	 * @param sheets
	 * @param inputArr
	 * @return
	 */
	private String[] judgeSelection_Jaccard2(String[] sheets, String[] inputArr)
	{
		inputArr = this.intToStrAnswer(sheets, inputArr);					//숫자형 답변을 선택지의 문자로 치환한다
		inputArr = this.ordinalToCardinal(sheets, inputArr);					//서수(첫, 두, 세, 네)를 기수(1, 2, 3, 4)로 치환한다

		String[][] rightWord = new String[inputArr.length][2];
		rightWord = enrollRightWord(sheets, inputArr, rightWord);		//새로 찾은 정답 단어를 등록한다

		int[][] rightWord2 = new int[inputArr.length][2];
		rightWord2 = enrollRightWord2(rightWord, rightWord2);		//새로 찾은 정답 단어를 숫자형으로 변환한다. 빈도가 같이 기록된다

		String[] result = makeResult(sheets, rightWord, rightWord2);

		return result;
	}


	private String findIndexOfWord(String[] sheets, String select)
	{
		String idx = "";
		for(int i=1; i<sheets.length; i++)
		{
			if(sheets[i].equals(select)) {
				idx = String.valueOf(i-1);
				break;
			}
		}

		return idx;
	}



}
