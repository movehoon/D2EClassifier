package kr.ac.hansung.d2e.D2EClassifier;

import com.google.gson.JsonObject;
import linguaFile.FileIO;
import rhino.RHINO;

public class Interface_Movements {
	static boolean findSimilarExpressionWithOrder = false;				//유사한 표현을 찾을 때 사전의 순서와 같은 표현만 찾을지 여부

	public String classsInterface(JsonObject jsonObject, FileIO fio, PreProcessor pp, FindExpression fx, RHINO rn, MakeQuestion mq, String dicType, String[] cklist, String[] ckVxlist, String[][] expression, String[][] expressArr, String[][] expressArr_Original, String[] objectlist, String[] lastObject, String[][] advlist)
	{
		String output = "NOT_FOUND";
		String input = jsonObject.get("input").getAsString();
		input = pp.splitComplexNounNVxOfInput(rn, dicType, input, fio, ckVxlist);		// 입력문에 대하여 복합명사와 보조용언 띄어쓰기를 수행합니다 (단, choice 블럭은 원문을 그대로 리턴)
		input = pp.GetInputArr_cklist(input, rn.ExternCall(input, true), cklist);				// input에서 cklist가 있는 어절만 모아 다시 String으로 만든다 (이 줄은 삭제해도 된다)

		//부사어 설정 부분
		input = fx.findNReplaceADV(input, rn, advlist);					//부사어를 찾고, 입력문에서는 삭제한다
		String foundAdvDesc = fx.concatAdvExp();


		//arrNum 설정부분. 완전일치를 아예 사용하지 않게 하려면 arrNum의 값을 -1을 준다
		boolean ok = this.ckGoodSentence(input, objectlist);		//적격 문장인지를 테스트한다.
		if(ok)
		{
			//int arrNum = -1;
			int arrNum = fx.findResponseExpression_Order_full(input, rn, expressArr, expressArr_Original, lastObject, objectlist);
			lastObject = fx.getFoundFirstObject();							//이 과정에서 발견된 목적어를 가져온다

			//일치하는 표현을 못 찾은 경우에 대하여
			if(arrNum==-1)
			{
				int[][] expressArrPart = null;
				if(findSimilarExpressionWithOrder) {				//사전의 순서와 같은 표현만 찾는 경우(시작 부분은 같아야 함)
					String[] foundKeyWords = fx.getFoundKeyWords();																											//Order_full 방법에서 발견된 주요 형태소
					expressArrPart = fx.findResponseExpression_Order_Part(input, rn, foundKeyWords, expressArr_Original, lastObject, objectlist);		//부분적으로 일치하는 표현들
					lastObject = fx.getFoundFirstObject();		//이 과정에서 발견된 목적어를 가져온다
				}
				else {														//사전의 순서와 달라도 있는 표현을 모두 찾는 경우 (일반적)
					expressArrPart = fx.findResponseExpression_noOrder(input, rn, expressArr_Original, lastObject, objectlist);
					lastObject = fx.getFoundFirstObject();		//이 과정에서 발견된 목적어를 가져온다
				}

				//발견된 요소 출력하기
				if(expressArrPart.length==0)
					System.out.println("\nNOT_FOUND");
				else
				{
					arrNum = fx.findMostHighObjAdv(expression, expressArrPart, objectlist, advlist, true, true);				//부사어의 목록과 목적어의 목록을 검사하여 가장 많은 배열의 넘버를 찾는다
					output = printFinalOutput(fx, expression, expressArrPart[arrNum][0], lastObject, foundAdvDesc);		//expressArrPart[arrNum][0] 대신 expressArrPart[0][0]을 택하면 무조건 첫 번째 것을 넣게 된다
				}
			}
			//일치하는 표현을 찾은 경우에 대하여
			else
			{
				output = printFinalOutput(fx, expression, arrNum, lastObject, foundAdvDesc);
			}
		}
		else
		{
			System.out.println("\nNOT_FOUND/Not-Good-Sentence");
		}

		return output.replaceAll("/+", "/");
	}


	/**
	 * 적격 문장인지를 테스트한다.
	 * @param input
	 * @param objectlist
	 * @return
	 */
	private boolean ckGoodSentence(String input, String[] objectlist)
	{
		boolean ok = true;
		ok = this.ckShortObjectSentence(input, objectlist);
		return ok;
	}


	/**
	 * 주요 목적어로만 이루어진 너무 짧은 문장이 아닌지 검사합니다.
	 * @param input
	 * @param objectlist
	 * @return
	 */
	private boolean ckShortObjectSentence(String input, String[] objectlist)
	{
		boolean ok = true;
		for(int i=0; i<objectlist.length; i++)
		{
			if(input.equals(objectlist[i]))
			{
				ok = false;
				System.out.print("\nNOT_FOUND/Not-Enough_Info");			//'손', '팔', '오른팔'과 같이 주요 목적어로만 너무 짧은 문장인 경우
				break;
			}
		}
		return ok;
	}


	/**
	 * 최종 출력
	 * @param fx
	 * @param expression
	 * @param arrNum
	 * @param lastObject
	 * @param foundAdvDesc
	 */
	private String printFinalOutput(FindExpression fx, String[][] expression, int arrNum, String[] lastObject, String foundAdvDesc)
	{
		if(foundAdvDesc.equals("/"))
			foundAdvDesc = "";

		String output = "NOT_FOUND";
		String a_col = expression[arrNum][0];
		String b_col = expression[arrNum][1];

		if(foundAdvDesc!=null)
			b_col = this.changeArm(foundAdvDesc, b_col);

		if(b_col.startsWith("DYN")||b_col.startsWith("ADV"))					// '더', '빨리' 등 부사였다면, 앞 명령어의 목적어를 가져온다
			b_col = fx.pasteLastObject_DYNADV(lastObject, b_col);

		System.out.println(a_col+"\t"+b_col+foundAdvDesc);			// 부사어가 사용되었다면 그 내용을 덧붙인다
		output = b_col+foundAdvDesc;

		return output;
	}



	/**
	 * 가리키는 방향이 270도 이상이면 오른팔을 왼팔로 바꿉니다.
	 * @param foundAdvDesc
	 * @param b_col
	 * @return
	 */
	private String changeArm(String foundAdvDesc, String b_col)
	{
		String[] adv = foundAdvDesc.split("/");

		if(b_col.contains("오른"))						//동작명세가 "오른팔"을 가지고 있을 때
		{
			for(int i=0; i<adv.length; i++)
			{
				if(adv[i].contains("DEG"))
				{
					String[] advdeg = adv[i].split("-");
					String degree = advdeg[1].replace("도", "");
					if(Integer.valueOf(degree) >= 270)
					{
						b_col = b_col.replace("오른", "왼");
						break;
					}
				}
			}
		}

		return b_col;
	}

}
