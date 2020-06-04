package kr.ac.hansung.d2e.D2EClassifier;

import java.util.ArrayList;
import java.util.List;

import linguaFile.FileIO;
import rhino.RHINO;


public class FindExpression
{
	private String[] cklist;
	private String[] advcklist = new String[] {"NNG", "NNP", "NNB", "NP", "NR", "VV", "VA", "VCN", "MM", "MAG", "MAJ", "IC", "XR", "SL", "SH", "NF", "NV", "SN", "겠", "ㄹ까", "을까", "ㄹ게"};
	private String[][] expressArr;									//stem 추출된 정렬 후 사전
	private String[][] expressArr_original;						//stem 추출된 정렬 전 사전
	private String[] foundKeyWords;							//탐색 과정 중 찾은 사전의 어휘들
	private String[] foundFirstObject;							//첫번째로 발견된 목적어 어휘
	private String foundDurationAdvExp;						//발견된 지연시간 부사어 결과 표현
	private String foundDirectionAdvExp;						//발견된 방향표현 부사어 결과 표현
	private ArrayList<String[]> foundGeneralAdvList;		//발견된 [0]일반 부사어, [1]동작명세, [2]형태소

	public void setFoundAdvList() {
		this.foundGeneralAdvList = new ArrayList<String[]>();
	}

	public FindExpression(String[] cklist)	{
		this.cklist = cklist;
	}

	public String[][] getExpressArr()	{
		return this.expressArr;
	}

	public String[][] getExpressArr_Original(){
		return this.expressArr_original;
	}

	public String[] getFoundKeyWords() {
		return this.foundKeyWords;
	}

	public String[] getFoundFirstObject() {
		return this.foundFirstObject;
	}

	private String getFoundDurationAdv() {
		return this.foundDurationAdvExp;
	}

	private String getFoundDirectionExp() {
		return this.foundDirectionAdvExp;
	}


	/**
	 * 이 클래스에서 찾은 2차원 arrayList의 부사어를 2차원 배열로 변환하여 가져온다
	 * @return
	 */
	public String[][] getFoundGeneralAdvList() {
		GeneralTools gt = new GeneralTools();
		String[][] foundAdv = gt.arrayList2DToArray2D(this.foundGeneralAdvList);
		return foundAdv;
	}


	/**
	 * 사전에서 어근과 추측형태소(겠,ㄹ까...)만을 추출하여 정렬하고, 정렬 전 사전도 만듭니다.
	 * @param rn 형태소분석기 객체
	 * @param fio FileIO 객체
	 * @param expression 원 사전
	 */
	public void sortExpressionDictionaryAndCopy(RHINO rn, FileIO fio, String[][] expression)
	{
		//사전의 내용을 형태소 분석한 뒤, stem 부분만 배열로 만들어서 전체를 2차원 배열로 저장하기
		String[][] expressArr = new String[expression.length][];											//바깥 배열(1차원)의 크기만 정해준다
		String[][] expressArr_original = new String[expression.length][];									//정렬 전 모습을 유지하는 배열도 생성한다.

		GetNeededMorph gm = new GetNeededMorph();

		for(int i=0; i<expressArr.length; i++)
		{
			String[] morph = gm.GetOutputPartArr_cklist(rn.ExternCall(expression[i][0], true), cklist);
			expressArr[i] = new String[morph.length];															//안쪽 배열(2차원)의 크기를 그때마다 정해준다.
			expressArr_original[i] = new String[morph.length+1];

			int col = 0;
			for(int j=0; j<expressArr[i].length; j++)
			{
				expressArr[i][j] = morph[j];
				expressArr_original[i][j] = morph[j];
				col++;
			}
			expressArr_original[i][col] = String.valueOf(i);				//정렬 전 모습을 유지하는 배열에 현재의 row(outer) 번호를 마지막에 기입한다.
		}

		gm.sort2DArrayByLength(expressArr, true);					//2차원 배열로 된 사전을 어근의 수로 정렬한다.

		// 아래 7줄은 위의 정렬 과정에서 empty가 된 배열을 삭제해준다
		List<String[]> list = new ArrayList<String[]>();
		for(int i=0; i<expressArr.length; i++)
		{
			if(expressArr[i].length != 0)
				list.add(expressArr[i]);
		}
		expressArr = list.toArray(new String[list.size()][2]);

		this.expressArr = expressArr;
		this.expressArr_original = expressArr_original;
	}


	/**
	 * 입력문과 stem 사전의 내용에서 서로 같은지를 비교하여 같은 정렬 전 사전의 배열 번호를 찾아줍니다.
	 * @param inputSentence 입력문
	 * @param rn 형태소분석기 객체
	 * @param expressArr stem정렬 후 사전
	 * @param expressArr_original stem정렬 전 사전
	 * @param objectlist 목적어 리스트
	 * @return
	 */
	public int findResponseExpression_Order_full(String inputSentence, RHINO rn, String[][] expressArr, String[][] expressArr_original, String[] lastObject, String[] objectlist)
	{
		GetNeededMorph gm = new GetNeededMorph();
		int arrNum = -1;
		String[] inputArr = gm.GetOutputPartArr_cklist(rn.ExternCall(inputSentence, true), cklist);

		String[] foundKeyWords = new String[inputArr.length];
		int kw = 0;

		boolean found = false;
		for(int i=0; i<expressArr.length; i++)											//모든 표현을 하나씩 조사한다.
		{
			for(int j=0; j<inputArr.length; j++)											//문장의 어느 부분에서부터 일치할지 알 수 없다.
			{
				try {
					if(inputArr[j].equals(expressArr[i][0]))									//표현의 첫 시작부분과 같아야 한다.
					{
						if(ckNotIncludedYet(foundKeyWords, expressArr[i][0])) 	//아직 입력되지 않은 어휘가 맞다면,
						{
							foundKeyWords[kw] = expressArr[i][0];						//발견한 것을 저장한다
							kw++;
						}

						int cntExpressSame = 1;											//표현의 숫자와 같은 수로 같게 되는지를 카운트
						if(cntExpressSame==expressArr[i].length)
						{
							arrNum = findOriginalArray(expressArr, expressArr_original, i);
							found = true;
							break;
						}
						else if(inputArr.length-1==j)										//현재 음절이 입력문의 마지막인데, 위에서 걸리지 않았다면, 다음 표현으로 가야 한다.
						{
							break;
						}
						else
						{
							j++;
							for(int k=1; k<expressArr[i].length && j<inputArr.length; k++, j++)		//사전의 길이 및 입력문의 길이보다 작을 때까지만 진행
							{
								if(inputArr[j].equals(expressArr[i][k]))
								{
									if(ckNotIncludedYet(foundKeyWords, expressArr[i][k])) 				//아직 입력되지 않은 어휘가 맞다면,
									{
										foundKeyWords[kw] = expressArr[i][k];									//발견한 것을 저장한다
										kw++;
									}

									cntExpressSame++;
									if(cntExpressSame==expressArr[i].length)
									{
										arrNum = findOriginalArray(expressArr, expressArr_original, i);
										found = true;
										break;
									}
								}
							}
							if(found) break;
						}
					}
				} catch(ArrayIndexOutOfBoundsException e)
				{
					// 사전을 재구성하면서 empty 배열이 생기게 된다. 이것을 접근하는 순간 에러가 발생하므로 try-catch 로 처리한다
					arrNum = -1;
				}
			}
			if(found)
				break;
		}

		this.ckHaveObject(inputArr, objectlist);											//발견된 첫번째 목적어를 클래스 변수에 담게 한다.
		this.foundKeyWords = gm.removeNullValue(foundKeyWords);		//null 원소가 있는 부분부터는 삭제하여 내보낸다.
		return arrNum;
	}


	/**
	 * 입력문과 핵심 어휘를 부분적으로 공유하는 사전의 표현들을 찾아줍니다. (시작은 동일해야 함)
	 * @param inputSentence
	 * @param rn
	 * @param foundKeyWords
	 * @param expressArr_Original
	 * @param lastObject
	 * @param objectlist
	 * @return
	 */
	public int[][] findResponseExpression_Order_Part(String inputSentence, RHINO rn, String[] foundKeyWords, String[][] expressArr_Original, String[] lastObject, String[] objectlist)
	{
		GetNeededMorph gm = new GetNeededMorph();
		String[] inputArr = gm.GetOutputPartArr_cklist(rn.ExternCall(inputSentence, true), cklist);
		int[][] expressArrOriginalCnt = new int[expressArr_Original.length][2];			//각 표현별로 일치하는 키워드의 수

		boolean hasObject = this.ckHaveObject(inputArr, objectlist);						//목적어를 가지고 있는지 확인한다
		if(!hasObject)																					//가지고 있지 않다면,
			inputArr = this.includeObject(inputArr, lastObject);								//이전의 목적어를 앞에 채워 넣는다.

		for(int i=0; i<expressArr_Original.length; i++)											//모든 표현을 하나씩 조사한다
		{
			for(int j=0; j<foundKeyWords.length; j++)											//발견된 키워드 어휘를 비교 조사한다
			{
				int expressArr2DLen = expressArr_Original[i].length;							//현재 사전이 갖고 있는 표현의 수
				for(int k=0; k<expressArr2DLen; k++)											//현재 표현의 모든 어휘에 대하여 조사한다
				{
					if(foundKeyWords[j].equals(expressArr_Original[i][k]))
					{
						expressArrOriginalCnt[i][0] = i;												//현재의 사전 배열 번호
						expressArrOriginalCnt[i][1] += 1;											//발견한 횟수를 저장한다
						break;
					}
				}
			}
		}

		this.ckHaveObject(inputArr, objectlist);													//발견된 첫번째 목적어를 클래스 변수에 담게 한다.
		return gm.remove0Value2D(this.sort2DArr(gm, expressArrOriginalCnt), 1);		//공통요소가 많은 것으로 내림차순 정렬하여 내보낸다;
	}


	/**
	 * 순서에 상관없이 입력문과 사전의 일치하는 표현을 찾는다
	 * @param inputSentence
	 * @param rn
	 * @param expressArr_Original
	 * @param lastObject
	 * @param objectlist
	 * @return
	 */
	public int[][] findResponseExpression_noOrder(String inputSentence, RHINO rn, String[][] expressArr_Original, String[] lastObject, String[] objectlist)
	{
		GetNeededMorph gm = new GetNeededMorph();
		String[] inputArr = gm.GetOutputPartArr_cklist(rn.ExternCall(inputSentence, true), cklist);
		int[][] expressArrOriginalCnt = new int[expressArr_Original.length][2];

		boolean hasObject = this.ckHaveObject(inputArr, objectlist);					//목적어를 가지고 있는지 확인한다
		if(!hasObject)																					//가지고 있지 않다면,
			inputArr = this.includeObject(inputArr, lastObject);								//이전의 목적어를 앞에 채워 넣는다.

		for(int i=0; i<expressArr_Original.length; i++)										//모든 표현을 하나씩 조사한다.
		{
			for(int j=0; j<inputArr.length; j++)													//문장의 어느 부분에서부터 일치할지 알 수 없다.
			{
				int expressArr2DLen = expressArr_Original[i].length;						//현재 사전이 갖고 있는 표현의 수
				for(int k=0; k<expressArr2DLen; k++)
				{
					if(inputArr[j].equals(expressArr_Original[i][k]))								//!!!여기서 같은 것을 검사하여 저장한다!!!
					{
						expressArrOriginalCnt[i][0] = i;											//현재의 사전 배열 번호

						boolean objFound = this.isObject(inputArr, j, objectlist);			//현재 배열이 목적어 리스트에 있는 것인지를 확인
						if(objFound)
							expressArrOriginalCnt[i][1] += 2;										//현재 배열이 목적어라면 +2를 저장한다
						else
							expressArrOriginalCnt[i][1] += 1;										//그렇지 않다면 발견한 횟수 +1만 저장한다
						break;
					}
				}
			}
		}

		this.ckHaveObject(inputArr, objectlist);													//발견된 첫번째 목적어를 클래스 변수에 담게 한다.
		int[][] testArr = this.sort2DArr(gm, expressArrOriginalCnt);						//2차원 배열을 숫자 크기에 따라 내림차순으로 정렬
		int[][] testArr_ordered = gm.remove0Value2D(testArr, 1);						//공통요소가 많은 것으로 내림차순 정렬하여 내보낸다
		int[][] testArr_high = gm.getHighNumber2D(testArr_ordered, 1, 2);			//공통요소가 가장 많은 그룹들만 추려낸다
		return testArr_high;
	}


	/**
	 * 현재 배열이 목적어 리스트에 있는 것인지를 확인한다
	 * @param inputArr
	 * @param currentArr
	 * @param objectlist
	 * @return
	 */
	private boolean isObject(String[] inputArr, int currentArr, String[] objectlist)
	{
		boolean objFound = false;
		for(int objNum=0; objNum<objectlist.length; objNum++)					//현재의 배열이 목적어 리스트에 있는지 검사
		{
			if(inputArr[currentArr].equals(objectlist[objNum]))
			{
				objFound = true;
				break;
			}
		}

		return objFound;
	}


	/**
	 * 사전에서 부사어(ADV)의 목록을 가져온다. [0]입력문에서의 원형태, [1]동작명세, [2]형태소분석된 형태
	 * @param rn
	 * @param movements_ADV
	 * @return
	 */
	public String[][] getAdvList(RHINO rn, String movements_ADV)
	{
		GetNeededMorph gm = new GetNeededMorph();
		GeneralTools gt = new GeneralTools();
		String[] movements_adv_arr = movements_ADV.split("\r\n");

		int advcnt = 0;
		for(int i=0; i<movements_adv_arr.length; i++)
		{
			String[] temp = movements_adv_arr[i].split(",");
			if(temp[1].startsWith("ADV"))
				advcnt++;																//우선 사전에서 ADV의 수를 파악한다
		}

		String[][] advlist = new String[advcnt][3];
		int cnt = 0;
		for(int i=0; i<movements_adv_arr.length; i++)
		{
			String[] temp = movements_adv_arr[i].split(",");
			if(temp[1].startsWith("ADV"))
			{
				String[] arr = gm.GetOutputPartArr_cklist(rn.ExternCall(temp[0], true), advcklist);

				advlist[cnt][0] = temp[0];											// 입력문에서의 원 형태
				advlist[cnt][1] = temp[1];											// 동작명세
				advlist[cnt][2] = gt.concatWithDelim(arr, "-");					// 형태소분석된 목록 (복수 형태소로 된 부사어는 "티-안-나" 처럼 존재)
				cnt++;
			}
		}

		return advlist;
	}



	/**
	 * 입력문에 다양한 수식어가 있는지를 확인하고, 있으면 수식어는 추출한 뒤 입력문에서 해당 수식어를 삭제한다
	 * @param inputSentence
	 * @param rn
	 * @param advlist
	 * @param foundGeneralAdvList
	 * @return
	 */
	public String findNReplaceADV(String inputSentence, RHINO rn, String[][] advlist)
	{
		inputSentence = inputSentence.replaceAll("\\s+", " ");
		GetNeededMorph gm = new GetNeededMorph();
		String[][] inputMorphArr = gm.GetOutputPartArr_cklist2D(rn.ExternCall(inputSentence, true), cklist);
		String[] durationExp = null;																//지연 시간 표현
		String[] directionExp = null;																//방향 표현

		if(inputMorphArr.length > 1)																//부사어만 홀로 있는 경우는 제외된다
		{
			//[지연시간표현]이 있었다면 시간을 담고, 해당표현이 삭제된 입력문을 받아온다
			//[0] 지연시간(숫자), [1] 지연시간단위(초, 분, 시간), [2] 지연시간표현이 삭제된 입력문
			durationExp = this.findDurationTime(inputSentence, inputMorphArr);
			inputSentence = durationExp[2].replaceAll("\\s+", " ");														//지연시간표현이 삭제된 입력문
			inputMorphArr = gm.GetOutputPartArr_cklist2D(rn.ExternCall(inputSentence, true), cklist);		//삭제된 표현으로 다시 형태소 분석
			this.foundDurationAdvExp = getFoundMeasureAdv("DUR", durationExp);								//지연시간 결과표현


			//[방향표현]이 있었다면 각도를 담고, 해당표현이 삭제된 입력문을 받아온다
			//[0] 방향(숫자 각도), [1] 방향표현단위(방향, 쪽, 도), [2] 방향표현이 삭제된 입력문
			directionExp = this.findDirection(inputSentence, inputMorphArr);
			inputSentence = directionExp[2].replaceAll("\\s+", " ");														//방향표현이 삭제된 입력문
			inputMorphArr = gm.GetOutputPartArr_cklist2D(rn.ExternCall(inputSentence, true), cklist);		//삭제된 표현으로 다시 형태소 분석
			this.foundDirectionAdvExp = getFoundMeasureAdv("DEG", directionExp);								//방향부사 결과표현


			//[일반 부사어] 찾기는 인터페이스가 다르다. 또한 다른 특수 부사어를 모두 삭제하고 난 다음에 마지막으로 적용해야 한다
			//복수의 부사어를 찾고, 그 부사어들을 삭제한 입력문을 출력한다 (예문: 오른팔을 빨리 뚜렷하게 위로 올려봐)
			//찾은 부사어는 fx.getFoundAdvList() 함수로 불러들일 수 있다
			inputSentence = this.findModifierSeveral(inputSentence, rn, advlist, "adv").replaceAll("\\s+", " ");
		}
		else
		{
			this.foundDurationAdvExp = "";
			this.foundDirectionAdvExp = "";
		}

		return inputSentence;
	}



	/**
	 * 발견된 다양한 종류의 부사어를 연결하여 최종 결과 표현형을 출력한다
	 * @return
	 */
	public String concatAdvExp()
	{
		String outputExp = "";

		String[][] foundGeneralAdv = this.getFoundGeneralAdvList();	//발견된 [0]일반 부사어, [1]동작명세, [2]형태소	를 2차원 일반 배열로 변환
		String foundDurationAdv = this.getFoundDurationAdv();			//발견된 시간지연 부사어 (3초 동안)
		String foundDirectionAdv = this.getFoundDirectionExp();		//발견된 방향지시 부사어 (3시 방향, 45도 방향)

		if(foundGeneralAdv.length>0||!foundDurationAdv.equals("")||!foundDirectionAdv.equals(""))
			outputExp = "/"+outputExp;

		//일반 부사어 연결
		for(int i=0; i<foundGeneralAdv.length; i++)
			outputExp = outputExp.concat(foundGeneralAdv[i][1])+"/";

		//시간지연 부사어 연결
		outputExp = outputExp.concat(foundDurationAdv)+"/";

		//방향표시 부사어 연결
		outputExp = outputExp.concat(foundDirectionAdv)+"/";

		//후처리
		outputExp = outputExp.replaceAll("/+", "/");
		if(outputExp.equals("/"))
			outputExp = "";

		return outputExp;
	}



	/**
	 * 단위 부사(지연시간, 방향표현 부사)의 출력용 표현을 가지고 온다
	 * @param measureName
	 * @param measure
	 * @return
	 */
	public String getFoundMeasureAdv(String measureName, String[] measureExp)
	{
		String outputExp = "";

		if(measureExp != null)
		{
			if(!measureExp[0].equals(""))			//단위부사표현이 사용된 경우에
				outputExp = measureName+"-"+measureExp[0]+measureExp[1];
		}

		return outputExp;
	}


	/**
	 * 복수의 수식어를 추출하고, 모든 수식어가 삭제된 입력문을 반환한다.
	 * 부사어 찾기를 염두에 두고 개발하였지만, modlist만 교체하면 원하는 수식어 종류를 찾을 수 있다.
	 * 예문: 오른팔을 빨리 뚜렷하게 위로 올려봐
	 * @param inputSentence 입력문
	 * @param rn 형태소분석기 객체
	 * @param modilist 사전에 있는 수식어의 목록
	 * @param modiType 찾으려고 하는 수식어의 종류
	 * @return
	 */
	private String findModifierSeveral(String inputSentence, RHINO rn, String[][] modilist, String modiType)
	{
		this.setFoundAdvList();													//찾은 부사어를 저장할 변수 초기화
		inputSentence = inputSentence.replaceAll("\\s+", " ");

		String inputSentence_old = "";
		GetNeededMorph gm = new GetNeededMorph();

		do
		{
			inputSentence_old = inputSentence;
			String[][] inputMorphArr = gm.GetOutputPartArr_cklist2D(rn.ExternCall(inputSentence, true), cklist);
			String[] inputEojulArr = inputSentence.split(" ");
			inputSentence = this.findModifierOne(inputSentence, modilist, inputMorphArr, inputEojulArr, modiType);
		}while(!inputSentence.equals(inputSentence_old));				// 더 이상 남은 수식어가 없을 때까지 반복

		return inputSentence;
	}


	/**
	 * 일반 수식어 찾기
	 * 부사어 찾기를 염두에 두고 개발하였지만, modlist와 modiType만 교체하면 원하는 수식어 종류를 찾을 수 있다.
	 * 예: 오른팔을 '빨리' 올려봐, 오른팔을 '많이 티 안나게' 위로 올려봐
	 * modiType이 "adv"인 경우 발견된 수식어는 this.foundAdvList에 add 되며, 반환값은 발견된 수식어가 삭제된 입력문이다
	 * @param inputSentence 입력문
	 * @param modilist 사전에 있는 수식어의 목록
	 * @param inputMorphArr 형태소분석된 입력문
	 * @param inputEojulArr 입력문의 단순 어절 분리
	 * @param modiType 찾으려고 하는 수식어의 종류. 현재는 adv 하나만 있음
	 * @return
	 */
	private String findModifierOne(String inputSentence, String[][] modilist, String[][] inputMorphArr, String[] inputEojulArr, String modiType)
	{
		boolean found = false;

		for(int i=0; i<modilist.length; i++)
		{
			String inputModiForm = "";
			String[] modilistArr = modilist[i][2].split("-");
			boolean foundFirst = false;				//수식어 배열의 첫 부분은 일치했는지 여부
			boolean notFirtStart = false;				//true이면 같은 부분이 있기는 하였으나, 첫 부분이 아니므로 빠져나와야 하는 경우

			for(int i2=0; i2<modilistArr.length; i2++)
			{
				int eojulStart = 0;
				//int i3 = i2;
				for(int j=0; j<inputMorphArr.length; j++)
				{
					if(modilistArr[i2].equals(inputMorphArr[j][0]))
					{
						if(i2==0)
						{
							foundFirst = true;		//처음은 같은 부분이 있음. 후보가 됨
							eojulStart = Integer.parseInt(inputMorphArr[j][1]);					//발견된 첫 부분의 형태 어절 번호를 기록
						}
						else
							notFirtStart = true;		//같은 부분이 있기는 하였으나, 첫 부분이 아니므로 빠져나와야 하는 경우

						if(foundFirst)
						{
							if(i2==modilistArr.length-1)												//해당 수식어의 형태를 모두 다 찾음
							{
								found = true;
								int eojulEnd = Integer.parseInt(inputMorphArr[j][1]);			//발견된 마지막 부분의 형태 어절 번호를 기록
								inputModiForm = this.pasteSpecificEojul(inputEojulArr, eojulStart, eojulEnd);	//어절 번호를 기준으로 수식어를 추출
								inputSentence = inputSentence.replace(inputModiForm.trim(), "").replaceAll("\\s+", " ");

								if(modiType.equals("adv"))												//찾는 수식어가 부사어인 경우
									this.foundGeneralAdvList.add(new String[] {modilist[i][0], modilist[i][1], modilist[i][2]});		//[0]은 발견된 수식어의 형태, [1]은 동작명세, [2]는 형태소

								break;				//완전히 찾았으면 전체 프로세스를 끝낸다
							}
							else						//만약 끝까지 왔다면 found=true가 되어 advlist에서 더 나갈 부분이 없을 것이다
								i2++;
						}
						else if(notFirtStart)		//같은 부분이 있기는 하였으나, 첫 부분이 아니므로 빠져나와야 하는 경우
							break;
					}
					else if(foundFirst)				//처음은 같은 부분이 있었으나, 모든 부분이 일치하지는 않았음
						break;
				}
				if(found)								//완전히 찾았으면 전체 프로세스를 끝낸다
					break;
				else if(foundFirst)					//처음은 같은 부분이 있었으나, 모든 부분이 일치하지는 않았음
					break;
				else if(notFirtStart)				//같은 부분이 있기는 하였으나, 첫 부분이 아니므로 빠져나와야 하는 경우
					break;
			}
			if(found)									//완전히 찾았으면 전체 프로세스를 끝낸다
				break;
		}

		return inputSentence;
	}


	/**
	 * 입력문에서 특정 어절 번호 안에 있는 것만을 가지고 옴
	 * @param inputEojulArr 찾을 입력문 어절 배열
	 * @param start 찾기 시작할 어절 번호(include)
	 * @param end 찾기 끝낼 어절 번호(include)
	 * @return
	 */
	public String pasteSpecificEojul(String[] inputEojulArr, int start, int end)
	{
		String result = "";

		for(int i=start; i<=end; i++)
			result = result.concat(inputEojulArr[i]+" ");

		return result;
	}


	/**
	 * 지연 시간 표현을 찾는다
	 * 지연시간("" 또는 숫자), 단위(초,분,시간), 지연시간표현이 삭제된 입력문을 결과로 보낸다
	 * @param inputSentence
	 * @param inputArr
	 * @return
	 */
	private String[] findDurationTime(String inputSentence, String[][] inputArr)
	{
		String[] resultArr = new String[3];
		String time = "";
		String unitExp = "";
		String regex = "^[0-9]+$";
		int durationStart = 0;
		int durationEnd = 0;

		boolean found = false;
		for(int i=0; i<inputArr.length; i++)
		{
			if(inputArr[i][0].matches(regex)&&ckDurDirOrder(inputArr, i))													// [1] 숫자가 발견되고,
			{
				time = inputArr[i][0];
				durationStart = this.findStartPosition(inputArr, inputArr[i][1]);

				for(int j=i; j<inputArr.length; j++)
				{
					if(findUnitExp(inputArr, i, inputArr.length, new String[]{"초","분","시간"}))								// [2] 뒤에 초, 분, 시간이 나온 경우에
					{
						if(findUnitExp(inputArr, i, j, new String[]{"동안"}))														//'5초 동안'의 '동안'을 찾음
						{
							unitExp = getUnitExp(inputArr, i, inputArr.length, new String[]{"초","분","시간"});
							int durationEndStart = inputSentence.indexOf(time);
							int unitExpArrIdx = findUnitExpIdx(inputArr, i, inputArr.length, new String[]{time});
							durationEnd = durationEndStart + this.findExpressionEndEnd(inputSentence, inputArr, unitExpArrIdx);
							found = true;
							break;
						}
						else if(findUnitExp(inputArr, i, j, new String[]{"간"}))														//'5초 간'의 '간'을 찾음
						{
							unitExp = getUnitExp(inputArr, i, inputArr.length, new String[]{"초","분","시간"});
							int durationEndStart = inputSentence.indexOf(time);
							int unitExpArrIdx = findUnitExpIdx(inputArr, i, inputArr.length, new String[]{time});
							durationEnd = durationEndStart + this.findExpressionEndEnd(inputSentence, inputArr, unitExpArrIdx);
							found = true;
							break;
						}
					}

				}
				if(found)
					break;
			}
		}

		if(found)																			//시간표현이 있었다면 입력문에서 해당 부분만 삭제한다
		{
			inputSentence = deleteDirectionMsg(inputArr, inputSentence, time, durationStart, durationEnd);
			inputSentence = inputSentence.replaceFirst("동안", "");
			inputSentence = inputSentence.replaceFirst("간", "");
		}

		if(found)																			//지연시간표현이 있었다면
		{
			resultArr[0] = time;
			resultArr[1] = unitExp;
			resultArr[2] = inputSentence;
		}
		else																				//지연시간표현이 발견되지 않았다면
		{
			resultArr[0] = "";
			resultArr[1] = "";
			resultArr[2] = inputSentence;
		}

		return resultArr;
	}


	private int findStartPosition(String[][] inputArr, String arrIdx)
	{
		int targetArrIdx = Integer.parseInt(arrIdx);
		int chrIdx = 0;

		for(int i=0; i<=targetArrIdx; i++)
			chrIdx += (inputArr[i].length + 1);

		return chrIdx;
	}



	/**
	 * 방향 표현을 찾는다
	 * "방향("" 또는 숫자), 단위(도), 방향표현이 삭제된 입력문"을 결과로 보낸다
	 * @param inputSentence
	 * @param inputArr
	 * @return
	 */
	private String[] findDirection(String inputSentence, String[][] inputArr)
	{
		String[] resultArr = new String[3];
		String direction = "";
		String unitExp = "";
		String regex = "^[0-9]+$";

		int directionStart = 0;
		int directionEnd = 0;

		boolean found = false;
		for(int i=0; i<inputArr.length; i++)
		{
			if(inputArr[i][0].matches(regex)&&ckDirDurOrder(inputArr, i))							// [1] 숫자가 발견되고,
			{
				direction = inputArr[i][0];
				directionStart = inputSentence.indexOf(inputArr[i][0]);
				for(int j=i; j<inputArr.length; j++)															//숫자가 발견된 이후부터만 찾는다
				{
					if(findUnitExp(inputArr, i, inputArr.length, new String[]{"방향","쪽"}))			// [2] 뒤에 '방향, 쪽'이 나온 경우
					{
						if(findUnitExp(inputArr, i, j, new String[]{"시"}))									//'4시 방향'의 '시'를 찾음
						{
							unitExp = "시";
							int unitExpArrIdx = findUnitExpIdx(inputArr, i, inputArr.length, new String[]{"방향","쪽"});
							int directionEndStart = inputSentence.indexOf(inputArr[unitExpArrIdx][0]);
							directionEnd = directionEndStart + this.findExpressionEndEnd(inputSentence, inputArr, unitExpArrIdx);
							found = true;
							break;
						}
						else if(findUnitExp(inputArr, i, j, new String[]{"도"}))								//'30도 방향'의 '도'를 찾음
						{
							unitExp = "도";
							int unitExpArrIdx = findUnitExpIdx(inputArr, i, inputArr.length, new String[]{"방향","쪽"});
							int directionEndStart = inputSentence.indexOf(inputArr[unitExpArrIdx][0]);
							directionEnd = directionEndStart + this.findExpressionEndEnd(inputSentence, inputArr, unitExpArrIdx);
							found = true;
							break;
						}
					}
					else
					{
						if(inputArr[j][0].equals("도"))															// '방향, 쪽' 표현 없이 쓰인 '30도 방향'의 '도'를 찾음
						{
							unitExp = "도";
							int unitExpArrIdx = findUnitExpIdx(inputArr, i, inputArr.length, new String[]{"도"});
							int directionEndStart = inputSentence.indexOf(inputArr[unitExpArrIdx][0]);
							directionEnd = directionEndStart + 1;
							found = true;
							break;
						}
					}
				}
				if(found)
					break;
			}
		}

		if(found)																					//방향표현이 있었다면 입력문에서 해당 부분만 삭제한다
			inputSentence = deleteDirectionMsg(inputArr, inputSentence, direction, directionStart, directionEnd);

		if(found)																					//방향표현이 있었다면
		{
			if(unitExp.equals("시"))															//"4시 방향"과 같이 사용되었다면.
				resultArr[0] = String.valueOf(Integer.parseInt(direction) * 30);	//4시 --> 120도 로 치환
			else if(unitExp.equals("도"))													//"30도 방향"과 같이 사용되었다면
				resultArr[0] = direction;
			else																					//그 외에는 모두 각도 표현인 "30도 방향"의 '도'가 사용되었다고 간주한다.
				resultArr[0] = direction;
			resultArr[1] = "도";
			resultArr[2] = inputSentence;
		}
		else																						//방향표현이 발견되지 않았다면
		{
			resultArr[0] = "";
			resultArr[1] = "";
			resultArr[2] = inputSentence;
		}

		return resultArr;
	}



	/**
	 * 방향이 시간표현보다 앞에 오는지를 확인한다
	 * @param inputArr
	 * @param arrNum
	 * @return
	 */
	private boolean ckDurDirOrder(String[][] inputArr, int arrNum)
	{
		boolean ok = true;

		for(int i=arrNum; i<inputArr.length; i++)
		{
			if(inputArr[i][0].equals("초")||inputArr[i][0].equals("분")||inputArr[i][0].equals("시간")||inputArr[i][0].equals("동안"))
			{
				ok = true;
				break;
			}
			else if(inputArr[i][0].equals("시")||inputArr[i][0].equals("방향")||inputArr[i][0].equals("쪽"))
			{
				ok = false;
				break;
			}
		}

		return ok;
	}


	/**
	 * 시간이 방향표현보다 앞에 오는지를 확인한다
	 * @param inputArr
	 * @param arrNum
	 * @return
	 */
	private boolean ckDirDurOrder(String[][] inputArr, int arrNum)
	{
		boolean ok = true;

		for(int i=arrNum; i<inputArr.length; i++)
		{
			if(inputArr[i][0].equals("초")||inputArr[i][0].equals("분")||inputArr[i][0].equals("시간")||inputArr[i][0].equals("동안"))
			{
				ok = false;
				break;
			}
			else if(inputArr[i][0].equals("시")||inputArr[i][0].equals("방향")||inputArr[i][0].equals("쪽"))
			{
				ok = true;
				break;
			}
		}

		return ok;
	}



	/**
	 * 시간/방향표시의 끝부분을 찾는다. (예: '오른팔을 4시 방향으로 올린다'에서 '방향으로'의 끝 위치)
	 * @param inputSentence
	 * @param inputArr
	 * @param unitExpArrIdx
	 * @return
	 */
	private int findExpressionEndEnd(String inputSentence, String[][] inputArr, int unitExpArrIdx)
	{
		int findDirectionEndLength=0;
		String[] inputSentenceArr = inputSentence.split(" ");

		for(int i=0; i<inputSentenceArr.length; i++)
		{
			if(inputSentenceArr[i].startsWith(inputArr[unitExpArrIdx][0]))
				findDirectionEndLength = inputSentenceArr[i].length();
		}

		return findDirectionEndLength;
	}


	/**
	 * 배열 i에서 j 사이에 특정 표현이 있는지를 확인한다
	 * @param inputArr
	 * @param i
	 * @param j
	 * @param unitExp
	 * @return
	 */
	private boolean findUnitExp(String[][] inputArr, int i, int j, String[] unitExp)
	{
		boolean found = false;
		for(int x=0; x<unitExp.length; x++)
		{
			for(int k=i; k<j; k++)
			{
				if(inputArr[k][0].equals(unitExp[x]))
				{
					found = true;
					break;
				}
			}
			if(found)
				break;
		}

		return found;
	}


	/**
	 * 배열 i에서 j 사이에 있는 특정 표현을 추출한다
	 * @param inputArr
	 * @param i
	 * @param j
	 * @param unitExp
	 * @return
	 */
	private String getUnitExp(String[][] inputArr, int i, int j, String[] unitExp)
	{
		String expression = "";
		boolean found = false;
		for(int x=0; x<unitExp.length; x++)
		{
			for(int k=i; k<j; k++)
			{
				if(inputArr[k][0].equals(unitExp[x]))
				{
					found = true;
					expression = unitExp[x];
					break;
				}
			}
			if(found)
				break;
		}

		return expression;
	}


	/**
	 * 배열 i에서 j 사이의 특정 표현이 있는 배열위치를 파악한다
	 * @param inputArr
	 * @param i
	 * @param j
	 * @param unitExp
	 * @return
	 */
	private int findUnitExpIdx(String[][] inputArr, int i, int j, String[] unitExp)
	{
		int arrIdx = 0;
		boolean found = false;

		for(int x=0; x<unitExp.length; x++)
		{
			for(int k=i; k<j; k++)
			{
				if(inputArr[k][0].equals(unitExp[x]))
				{
					arrIdx = k;
					found = true;
					break;
				}
			}
			if(found)
				break;
		}

		return arrIdx;
	}


	/**
	 * 입력문에서 방향표시 부분을 삭제한다
	 * @param inputArr
	 * @param inputSentence
	 * @param direction
	 * @param directionStart
	 * @param directionEnd
	 * @return
	 */
	private String deleteDirectionMsg(String[][] inputArr, String inputSentence, String direction, int directionStart, int directionEnd)
	{
		if(directionStart !=0 && directionStart != -1 && directionEnd !=0 && directionEnd != -1 && directionEnd > directionStart)
		{
			String directionMsg = "";
			directionMsg = inputSentence.substring(directionStart, directionEnd);
			inputSentence = inputSentence.replace(directionMsg, "");
		}

		return inputSentence;
	}


	/**
	 * 목적어가 있는지를 체크하고, 목적어와 그 수식어를 클래스 변수에 담는다
	 * @param inputArr	입력문 배열
	 * @param objectlist	목적어 후보 리스트
	 * @return
	 */
	private boolean ckHaveObject(String[] inputArr, String[] objectlist)
	{
		boolean found = false;

		for(int i=0; i<inputArr.length; i++)											//모든 표현을 하나씩 조사한다.
		{
			for(int j=0; j<objectlist.length; j++)
			{
				if(inputArr[i].equals(objectlist[j]))										//목적어에 해당하는 것이 존재하면
				{
					found = true;
					this.foundFirstObject = makeObjectArr(inputArr, i);		//발견된 첫번째 목적어를 클래스 변수에 담는다
					break;
				}
			}
			if(found)
				break;
		}

		return found;
	}


	/**
	 * 목적어와 그 수식어를 찾아 배열로 만든다
	 * @param inputArr	입력문 배열
	 * @param objNum	목적어가 있는 배열 번호
	 * @return
	 */
	private String[] makeObjectArr(String[] inputArr, int objNum)
	{
		String[] objArr = null;

		if(objNum==0)													//발견된 목적어가 배열의 처음에 있어서 더 이상 수식어가 있을 수 없는 경우
		{
			objArr = new String[1];
			objArr[0] = inputArr[objNum];
		}
		else if(objNum==1)												//발견된 목적어가 배열의 두 번째에 있어서 그 앞에 하나의 수식어가 있을 수 있는 경우
		{
			if(inputArr[objNum].equals("눈")||inputArr[objNum].equals("팔")||inputArr[objNum].equals("손")||inputArr[objNum].equals("쪽"))
			{
				if(inputArr[objNum-1].equals("왼")||inputArr[objNum-1].equals("오른")||inputArr[objNum-1].equals("양")||inputArr[objNum-1].equals("위")||inputArr[objNum-1].equals("아래"))
				{
					objArr = new String[2];
					objArr[0] = inputArr[objNum-1];					//왼, 오른, ...
					objArr[1] = inputArr[objNum];					//눈, 팔, 손
				}
				else															//수식어가 없으면 현재의 목적어만 기록한다
				{
					objArr = new String[1];
					objArr[0] = inputArr[objNum];					//눈, 팔, 손
				}
			}
		}
		else																	//발견된 목적어가 배열의 세 번째 이하에 있어서 그 앞에 두 개의 수식어도 있을 수 있는 경우
		{
			if(inputArr[objNum].equals("눈")||inputArr[objNum].equals("팔")||inputArr[objNum].equals("손")||inputArr[objNum].equals("쪽"))
			{
				if(inputArr[objNum-1].equals("쪽"))					//당연히 왼, 오른 등의 수식어가 있을 것이다
				{
					objArr = new String[3];
					objArr[0] = inputArr[objNum-2];					//왼, 오른, ...
					objArr[1] = inputArr[objNum-1];					//쪽
					objArr[2] = inputArr[objNum];					//눈, 팔, 손
				}
				else if(inputArr[objNum-1].equals("왼")||inputArr[objNum-1].equals("오른")||inputArr[objNum-1].equals("양")||inputArr[objNum-1].equals("위")||inputArr[objNum-1].equals("아래"))
				{
					objArr = new String[2];
					objArr[0] = inputArr[objNum-1];					//왼, 오른, ...
					objArr[1] = inputArr[objNum];					//눈, 팔, 손
				}
				else															//수식어가 없으면 현재의 목적어만 기록한다
				{
					objArr = new String[1];
					objArr[0] = inputArr[objNum];					//눈, 팔, 손
				}
			}
		}

		return objArr;
	}

	/**
	 * 입력된 배열의 앞에 새로운 어휘를 추가합니다
	 * @param inputArr	기존 배열
	 * @param lastObject 추가할 어휘
	 * @return
	 */
	private String[] includeObject(String[] inputArr, String[] newWord)
	{
		if(newWord != null)																				//newWord가 null이 아닌 경우에만
		{
			int objArrLen = newWord.length;

			if(objArrLen==1)
			{
				if(newWord[0] != null)																	//newWord 0이 null이 아닌 경우에만
				{
					inputArr = (String[])resizeArray(inputArr, inputArr.length+1, 1);			//맨 앞에 빈 자리를 둔 채 현재 배열의 길이를 하나 늘리고,
					inputArr[0] = newWord[0];															//이전 대화의 목적어를 처음에 입력한다
				}
			}
			else if(objArrLen==2)
			{
				if(newWord[1] != null)																	//newWord 1이 null이 아닌 경우에만
				{
					inputArr = (String[])resizeArray(inputArr, inputArr.length+2, 2);			//맨 앞에 빈 자리를 둔 채 현재 배열의 길이를 하나 늘리고,
					inputArr[0] = newWord[0];															//이전 대화의 목적어를 처음에 입력한다
					inputArr[1] = newWord[1];															//이전 대화의 목적어 수식어를 입력한다
				}
			}
			else if(objArrLen>2)
			{
				if(newWord[2] != null)																	//newWord 2가 null이 아닌 경우에만
				{
					inputArr = (String[])resizeArray(inputArr, inputArr.length+3, 3);			//맨 앞에 빈 자리를 둔 채 현재 배열의 길이를 하나 늘리고,
					inputArr[0] = newWord[0];															//이전 대화의 목적어를 처음에 입력한다
					inputArr[1] = newWord[1];															//이전 대화의 목적어 수식어를 입력한다
					inputArr[2] = newWord[2];															//이전 대화의 목적어 수식어를 입력한다
				}
			}
		}

		return inputArr;
	}

	/**
	 * 원소를 하나 더 넣을 수 있도록 inputArr의 크기를 늘립니다
	 * @param oldArray 사용하던 inputArr
	 * @param newSize 크기가 늘어난 inputArr
	 * @param newArrStart 새로운 inputArr을 채울 번호
	 * @return
	 */
	private static Object resizeArray (Object oldArray, int newSize, int newArrStart)
	{
		int oldSize = java.lang.reflect.Array.getLength(oldArray);
		Class<?> elementType = oldArray.getClass().getComponentType();
		Object newArray = java.lang.reflect.Array.newInstance(elementType, newSize);
		int preserveLength = Math.min(oldSize, newSize);
		if (preserveLength > 0)
			System.arraycopy(oldArray, 0, newArray, newArrStart, preserveLength);
		return newArray;
	}


	/**
	 * 아직 입력되지 않은 어휘가 맞는지를 확인한다
	 * @param foundKeyWords
	 * @param word
	 * @return
	 */
	private boolean ckNotIncludedYet(String[] foundKeyWords, String word) {
		boolean notFound = true;

		for(int i=0; i<foundKeyWords.length; i++)
		{
			if(foundKeyWords[i]==null)
			{
				break;					//현재 배열이 null이라면 더 검사할 필요가 없다
			}
			else if(foundKeyWords[i].equals(word))
			{
				notFound = false;
				break;
			}
		}

		return notFound;
	}



	/**
	 * 2차원 배열을 n번째 요소([n])의 숫자 크기에 따라 내림차순으로 정렬합니다.
	 * @param expreeArrCnt
	 * @return
	 */
	public int[][] sort2DArr(GetNeededMorph gm, int[][] expreeArrCnt)
	{
		gm.sort2DArrayBy2ndNum(expreeArrCnt, 1, true);

		return expreeArrCnt;
	}


	/**
	 * 후보 표현들을 출력한다.
	 * @param expressArrPart
	 * @param expression
	 * @param foundAdvDesc
	 */
	public void printCandidateExpression(int[][] expressArrPart, String[][] expression, String foundAdvDesc)
	{
		int foundNum = expressArrPart[0][1];
		System.out.println("공통된 요소가 "+foundNum+"개 발견된 표현들");

		for(int i=0; i<expressArrPart.length; i++)
		{
			if(expressArrPart[i][1]<foundNum)
			{
				System.out.println("\n공통된 요소가 "+expressArrPart[i][1]+"개 발견된 표현들");
				foundNum = expressArrPart[i][1];
			}
			System.out.println(expressArrPart[i][0]+"번 : " + expression[expressArrPart[i][0]][0] + "\t부사어: " + foundAdvDesc);
		}
	}

	/**
	 * 정렬된 배열의 원모습을 갖고 있는 배열의 번호를 찾습니다.
	 * @param sortedArr
	 * @param originalArr
	 * @param foundNum
	 * @return
	 */
	private int findOriginalArray(String[][] sortedArr, String[][] originalArr, int foundNum)
	{
		String foundArrNum = null;
		boolean wholeSame = false;

		for(int i=0; i<originalArr.length; i++)
		{
			int innerSameCnt = 0;
			for(int j=0; j<originalArr[i].length; j++)
			{
				if(!originalArr[i][j].equals(sortedArr[foundNum][j]))						//같지 않은 것이 발견되면 중단
					break;
				else
					innerSameCnt++;															//같다면 카운트

				if(innerSameCnt==sortedArr[foundNum].length)						//sortedArr의 끝에 도달
				{
					if(((originalArr[i].length)-1)==sortedArr[foundNum].length)
					{
						wholeSame = true;
						foundArrNum = originalArr[i][originalArr[i].length-1];		//원 배열 정보가 기록된 것을 가져온다
						break;
					}
					else
						break;
				}
			}
			if(wholeSame)
				break;
		}

		return Integer.valueOf(foundArrNum);
	}


	/**
	 * 목적어와 부사어가 가장 많은 배열의 넘버를 가지고 옵니다.
	 * @param expression
	 * @param highArrNum
	 * @param objlist
	 * @param advlist
	 * @param useObjScore
	 * @param useAdvScore
	 * @return
	 */
	public int findMostHighObjAdv(String[][] expression, int[][] highArrNum, String[] objlist, String[][] advlist, boolean useObjScore, boolean useAdvScore)
	{
		int mostHighArrNum = 0;											//현재 가장 많은 목적어와 부사어를 가지고 있는 배열
		int currentHighScore = 0;											//현재 가장 많은 목적어와 부사어의 수

		for(int i=0; i<highArrNum.length; i++)
		{
			String dic_a_col = expression[highArrNum[i][0]][0];		//형태소 분석된 사전의 내용
			int objscore = 0;
			int advscore = 0;

			if(useObjScore)													//사전에서 목적어가 많은 것을 우선 선택
			{
				String dic_a_col_copyA = dic_a_col;					//목적어 탐색을 위한 카피본
				for(int j=0; j<objlist.length; j++)
				{
					if(dic_a_col_copyA.contains(objlist[j]))
					{
						objscore++;
						dic_a_col_copyA = dic_a_col_copyA.replace(objlist[j], "");
					}
				}
			}


			if(useAdvScore)													//사전에서 부사어가 많은 것을 우선 선택
			{
				String dic_a_col_copyB = dic_a_col;						//부사어 탐색을 위한 카피본
				for(int j=0; j<advlist.length; j++)
				{
					if(dic_a_col_copyB.contains(advlist[j][0]))
					{
						advscore++;
						dic_a_col_copyB = dic_a_col_copyB.replace(advlist[j][0], "");
					}
				}
			}


			int tempHighScore = objscore + advscore;
			if(tempHighScore > currentHighScore)
			{
				mostHighArrNum = i;
				currentHighScore = tempHighScore;
			}
		}

		return mostHighArrNum;
	}


	public String pasteLastObject_DYNADV(String[] lastObject, String dic_B_col)
	{
		String object = "";

		if(lastObject != null)
		{
			for(int i=0; i<lastObject.length; i++)
				object = object.concat(lastObject[i]);
			dic_B_col = dic_B_col.concat("-"+object);
		}

		return dic_B_col;
	}


	/**
	 * 극성을 반대로 바꿔줄 표현이 있는지 확인하여 있으면 바꿔준다
	 * @param input
	 * @param arrNum
	 * @param expression
	 * @param expressArr_Original
	 * @return
	 */
	public String[][] findAdverseExpression(String input, int arrNum, String[][] expression, String[][] expressArr_Original)
	{
		boolean found = false;
		if(input.contains("않"))														//입력문에 '않' 표현이 있었다면
		{
			for(int i=0; i<expressArr_original[arrNum].length; i++)
			{
				if(expressArr_original[arrNum][i].equals("않"))					//그런데 발견된 사전의 내용에도 '않' 표현이 있었다면
				{
					found = true;														//발견표시하고, 더 특별한 처리를 하지 않는다
					break;
				}
			}

			if(!found)																	//그러나 입력문에는 '않'이 있었으나, 사전에는 없었다면
			{
				if(expression[arrNum][1].trim().equals("긍정"))
					expression[arrNum][1] = "부정";
				else if(expression[arrNum][1].trim().equals("부정"))
					expression[arrNum][1] = "긍정";
			}
		}

		return expression;
	}

}
