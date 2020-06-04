package kr.ac.hansung.d2e.D2EClassifier;

import java.util.ArrayList;

import linguaFile.FileIO;
import rhino.RHINO;


public class PreProcessor
{
	/**
	 * 입력문에 대하여 복합명사와 보조용언 띄어쓰기를 수행합니다.
	 * 단, choice 블럭 등에 대하여는 이러한 띄어쓰기 작업을 수행하지 않고, 원본을 그대로 다시 리턴합니다.
	 * @param rn
	 * @param dicType
	 * @param input
	 * @param fio
	 * @param ckVxlist
	 * @return
	 */
	public String splitComplexNounNVxOfInput(RHINO rn, String dicType, String input, FileIO fio, String[] ckVxlist)
	{
		if(dicType.equals("choice"))
		{
			;																// choice에 대해서는 아무 작업도 하지 않는다
		}
		else if(dicType.equals("picknum"))
		{
			input = this.splitNumberNoun(input, fio);
		}
		else
		{
			input = this.splitComplexNoun(input, fio);		//입력문에 띄어써야 할 복합명사가 있는 경우, 띄운다.
			input = this.splitVx(rn, input, ckVxlist);				//입력문에 보조용언이 있는 경우, 띄운다.
			input = this.changeNumExpToIntExp(input);
		}
		return input;
	}


	/**
	 * 입력문에 보조용언이 본용언과 붙어있는 경우, 이를 띄어쓴다.
	 * @param rn
	 * @param input 입력문
	 * @param cklist 띄어쓸 보조용언 목록
	 * @return
	 */
	private String splitVx(RHINO rn, String input, String[] cklist)
	{
		String[] suspiciousVx = {"나", "내", "마", "서", "섭", "주", "줄", "지", "질", "집", "한", "본", "쳐", "쳤", "치", "친"};					//이 음절들은 보조용언이 아닐 수 있으므로 확인을 해야 한다.

		for(int i=0; i<cklist.length; i++)							//모든 체크리스트에 대하여 수행한다.
		{
			if(input.contains(cklist[i]))								//체크리스트 음절이 있다면
			{
				int chIdx = input.indexOf(cklist[i]);
				if(chIdx==0)											//그 음절이 시작 음절이면 해당없음
					;
				else
				{
					char ch = input.charAt(chIdx-1);
					if(ch==' ')											//그 음절의 한 음절 앞의 것이 공백이면 해당없음
						;
					else													//공백이 아니라면, 공백을 만들어줌
					{
						if(this.ckIsVx_forSuspicious(rn, input, chIdx, suspiciousVx))				//확인 대상 보조용언이 아니거나, 확인 대상 보조용언이면서 검사를 통과했다면
							input = input.substring(0, chIdx) + " " + input.substring(chIdx, input.length());
					}
				}
			}
		}

		return input;
	}



	/**
	 * 보조용언이 맞는지 확인하는 함ㅅ
	 * @param rn
	 * @param input
	 * @param chIdx
	 * @param suspiciousVx
	 * @return
	 */
	private boolean ckIsVx_forSuspicious(RHINO rn, String input, int chIdx, String[] suspiciousVx)
	{
		boolean isVx = true;
		String[] conEomi = {"어", "아", "여", "야", "려", "고", "곤", "게", "지"};
		for(int i=0; i<suspiciousVx.length; i++)
		{
			if(String.valueOf(input.charAt(chIdx)).equals(suspiciousVx[i]))				//현재 음절이 보조용언이 맞는지 의심되는 음절이 맞다면
			{																							//의심음절이 아니면 isVx = true가 된다
				isVx = false;																		//일단 결과를 false로 돌려놓는다.
				for(int j=0; j<conEomi.length; j++)
				{
					if(String.valueOf(input.charAt(chIdx-1)).equals(conEomi[j]))		//그 앞 음절이 연결어미라면
					{
						String output = rn.ExternCall(input, true);
						if(this.ckIsVx_byTag(input, output, chIdx))							//품사 기반의 보조용언 테스트
						{
							isVx = true;
							break;
						}
					}
				}
				if(isVx)																				//찾았으면 바로 종료한다.
					break;
			}
		}

		return isVx;
	}


	/**
	 * 띄어쓸 복합명사 리스트를 참조하여 입력문 또는 사전의 내용을 띄어씁니다.
	 * @param input
	 * @return
	 */
	private String splitComplexNoun(String input, FileIO fio)
	{
		String[][] ckComplexNoun = fio.makeFileAllContents2DArray("./dic/com/", "cklist_complexNoun.txt", "UTF8", 2);

		for(int i=0; i<ckComplexNoun.length; i++)
		{
			if(input.contains(ckComplexNoun[i][0]))
				input = input.replaceAll(ckComplexNoun[i][0], ckComplexNoun[i][1]);
		}

		return input;
	}


	/**
	 * 띄어쓸 숫자 리스트를 참조하여 입력문 또는 사전의 내용을 띄어씁니다.
	 * @param input
	 * @param fio
	 * @return
	 */
	private String splitNumberNoun(String input, FileIO fio)
	{
		String[][] cklist = fio.makeFileAllContents2DArray("./dic/com/", "cklist_picknum.txt", "euc-kr", 2);

		for(int i=0; i<cklist.length; i++)
		{
			if(input.contains(cklist[i][0]))
				input = input.replaceAll(cklist[i][0], cklist[i][1]);
		}

		return input;
	}


	/*
	 * 한글형 숫자시간을 아라비아 숫자시간 표현으로 바꾼다
	 */
	private String changeNumExpToIntExp(String input)
	{
		input = input.replace("한시", "1시");
		input = input.replace("두시", "2시");
		input = input.replace("세시", "3시");
		input = input.replace("네시", "4시");
		input = input.replace("다섯시", "5시");
		input = input.replace("여섯시", "6시");
		input = input.replace("일곱시", "7시");
		input = input.replace("여덟시", "8시");
		input = input.replace("아홉시", "9시");
		input = input.replace("열시", "10시");
		input = input.replace("열한시", "11시");
		input = input.replace("열두시", "12시");

		input = input.replace("한 시", "1시");
		input = input.replace("두 시", "2시");
		input = input.replace("세 시", "3시");
		input = input.replace("네 시", "4시");
		input = input.replace("다섯 시", "5시");
		input = input.replace("여섯 시", "6시");
		input = input.replace("일곱 시", "7시");
		input = input.replace("여덟 시", "8시");
		input = input.replace("아홉 시", "9시");
		input = input.replace("열 시", "10시");
		input = input.replace("열한 시", "11시");
		input = input.replace("열두 시", "12시");
		return input;
	}


	/**
	 * 사전의 형태를 분석에 맞게 변환합니다.
	 * 단, choice 블럭에 대해서는 사전의 형태를 변환하지 않습니다.
	 * @param rn
	 * @param originalDicPath
	 * @param dictionary
	 * @param cklist
	 * @param ckPicknumlist
	 * @param encoding
	 * @return
	 */
	public String cleanDictionary(RHINO rn, String originalDicPath, String dictionary, String[] cklist, String[] ckPicknumlist, String encoding)
	{
		FileIO fio = new FileIO();
		String[][] expression = fio.makeFileAllContents2DArrayBySign(originalDicPath, dictionary, encoding, 2, ",");

		if(dictionary.equals("choice.CSV"))				//choice.CSV 등 일부 사전은 아래의 복합명사 분리, 보조용언 분리를 하지 않는다.
			;
		else if(dictionary.equals("picknum.CSV"))		//picknum.CSV 의 띄어써야 할 숫자 분리
		{
			for(int i=0; i<expression.length; i++)
				expression[i][0] = this.splitNumberNoun(expression[i][0].replaceAll("\\s+", " "), fio);
		}
		else														//그외 사전(movements, yesno)에 대해서는 복합명사 분리, 보조용언 분리를 진행한다.
		{
			for(int i=0; i<expression.length; i++)
				expression[i][0] = this.splitComplexNoun(expression[i][0].replaceAll("\\s+", " "), fio);		//띄어써야 할 복합명사 분리. 오른팔 --> 오른 팔

			for(int i=0; i<expression.length; i++)
				expression[i][0] = this.splitVx(rn, expression[i][0].replaceAll("\\s+", " "), cklist);				//보조용언 분리
		}

		StringBuilder sb = new StringBuilder();
		for(int i=0; i<expression.length; i++)
			sb.append(expression[i][0]+","+expression[i][1]+"\r\n");

		return sb.toString();
	}


	/**
	 * 문장부호를 기준으로 문장을 나누어 복수의 문장으로 만든다
	 * @param input
	 * @return
	 */
	public String[] separateSentence(String input)
	{
		ArrayList<String> inputAL = new ArrayList<String>(3);

		String[] inputs = input.split("\\.|\\?|!|,");							// 문장 분리

		for(String ip : inputs)											// 분리된 문장을 ArrayList에 담는다
			inputAL.add(ip);

		String[] sentences = new String[inputAL.size()];			// 분리된 문장의 크기로 배열을 만든다

		for(int i=0; i<inputAL.size(); i++)
			sentences[i] = inputAL.get(i);

		return sentences;
	}


	/**
	 * input sentence에서 cklist를 하나 이상 포함하고 있는 어절만 취합니다
	 * 만약 이 후에 다시 형태소분석을 하게 되면 정보가 부족하여 형태소분석이 적절히 되지 않을 수도 있습니다
	 * @param input
	 * @param output
	 * @param cklist
	 * @return
	 */
	public String GetInputArr_cklist(String input, String output, String[] cklist)
	{
		GetNeededMorph gm = new GetNeededMorph();
		String[] outputPartArr = new String[gm.GetMorphLength(output)];
		String[] outputArr = gm.GetOutputArr();

		int outputArrLength = 0;
		for(int i=0; i<outputArr.length; i++)
		{
			if(outputArr[i].equals(""))
				;
			else
				outputArrLength++;
		}

		for(int i=0, m=0; i<outputArrLength; i++, m++)
		{
			String[] part1 = outputArr[i].split("\t");
			String eojul = part1[0];
			boolean found = false;

			try{
				String[] part2 = part1[1].split(" \\+ ");
				for(int i2=0; i2<part2.length; i2++)							//part2는 "별로/MAG" 와 같은 형태로 되어 있다.
				{
					for(int j=0; j<cklist.length; j++)							//검사해야 할 리스트를 순회한다.
					{
						if(part2[i2].contains(cklist[j]))
						{
							found = true;
							outputPartArr[m] = eojul;
							if(i2<part2.length-1)									//마지막에는 m 값을 올리지 않는다.
								m++;
							break;
						}
					}
					if(found)
						break;
				}
			}catch(Exception e){ System.out.println("GetInputArr_cklist()에서 오류가 발생했습니다."); e.printStackTrace();}
		}

		outputPartArr = gm.removeNullValue(outputPartArr);

		//배열로 된 것을 다시 String으로 돌리는 과정
		//만약, 배열 상태를 원한다면 아래 3줄을 지우고, return outputPartArr; 을 하면 된다
		String result = "";
		for(int i=0; i<outputPartArr.length; i++)
			result = result.concat(outputPartArr[i]+ " ");

		return result.trim();
	}



	/**
	 * 품사 정보 확인을 통해 해당 음절이 보조용언이 맞는지를 정확히 검사합니다
	 * @param input 사전 입력형
	 * @param output 형태소 분석결과
	 * @param chIdx 보조용언 여부를 검사하려고 하는 문자열의 index
	 */
	private boolean ckIsVx_byTag(String input, String output, int chIdx)
	{
		//String investingChr = Character.toString(input.charAt(chIdx));					//조사 대상 음절
		boolean found = false;

		GetNeededMorph gm = new GetNeededMorph();
		@SuppressWarnings("unused")
		String[][] outputPartArr = new String[gm.GetMorphLength(output)][2];
		String[] outputArr = gm.GetOutputArr();

		int outputArrLength = 0;
		for(int i=0; i<outputArr.length; i++)
		{
			if(outputArr[i].equals(""))
				;
			else
				outputArrLength++;
		}

		int curChrIdx = 0;    												//현재 어절까지의 문자 인덱스(조사 대상 음절을 확인하기 위한)
		for(int i=0; i<outputArrLength; i++)
		{
			String[] part1 = outputArr[i].split("\t");
			String eojul = part1[0];
			curChrIdx += eojul.length();

			try{
				if(chIdx < curChrIdx)											//찾으려는 음절이 현재 배열에 속해 있으면
				{
					String[] part2 = part1[1].split(" \\+ ");
					for(int i2=1; i2<part2.length; i2++)					//part2는 "별로/MAG" 와 같은 형태로 되어 있다. 0번이 아닌 1번부터 검사
					{
						if(part2[i2].contains("/"))								//정상적인 경우
						{
							if(part2[i2].contains("VX")||part2[i2].contains("VV")||part2[i2].contains("VA"))		//보조용언 또는 동사, 형용사를 가지고 있다면
							{
								if(part2[i2-1].contains("EC")||part2[i2-1].contains("EF"))		// 그 앞 형태소가 어미류라면,
								{
									found = true;									//0번이 아닌 1번 이후인데 VX, VV, VA가 있었고, 그 앞에는 어미가 있었다면 분리되지 않은 보조용언으로 본다
									break;
								}
							}
						}
						else														//예외처리
							;
					}
					if(found)
						break;
				}
				curChrIdx++;										//어절이 넘어갈 때마다 공백 1자를 반영한다
			}catch(Exception e){ System.out.println("GetOutputPartArr_cklist2D()에서 오류가 발생했습니다."); e.printStackTrace();}
		}

		return found;
	}



}
