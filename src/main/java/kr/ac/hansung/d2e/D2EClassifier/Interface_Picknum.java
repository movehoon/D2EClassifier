package kr.ac.hansung.d2e.D2EClassifier;

import com.google.gson.JsonObject;
import linguaFile.FileIO;
import rhino.RHINO;

public class Interface_Picknum
{
	static boolean findSimilarExpressionWithOrder = false;				//유사한 표현을 찾을 때 사전의 순서와 같은 표현만 찾을지 여부
	String[] sheets = null;

	public String classsInterface(JsonObject jsonObject, FileIO fio, PreProcessor pp, FindExpression fx, RHINO rn, MakeQuestion mq, String dicType, String[] cklist, String[] ckVxlist, String[][] expression, String[][] expressArr, String[][] expressArr_Original, String[] objectlist, String[] lastObject, String[][] advlist)
	{
		String output = "NOT_FOUND";
		String input = jsonObject.get("input").getAsString();
		input = pp.splitComplexNounNVxOfInput(rn, dicType, input, fio, ckVxlist);		// 입력문에 대하여 복합명사와 보조용언 띄어쓰기를 수행합니다 (단, choice 블럭은 원문을 그대로 리턴)
		input = pp.GetInputArr_cklist(input, rn.ExternCall(input, true), cklist);				// input에서 cklist가 있는 어절만 모아 다시 String으로 만든다 (이 줄은 삭제해도 된다)

		//부사어 설정 부분
		//input = fx.findNReplaceADV(input, rn, advlist);					//부사어를 찾고, 입력문에서는 삭제한다
		//String foundAdvDesc = fx.concatAdvExp();

		//arrNum 설정부분. 완전일치를 아예 사용하지 않게 하려면 arrNum의 값을 -1을 준다
		int arrNum = fx.findResponseExpression_Order_full(input, rn, expressArr, expressArr_Original, lastObject, objectlist);
		lastObject = fx.getFoundFirstObject();								//이 과정에서 발견된 목적어를 가져온다

		//일치하는 표현을 못 찾은 경우에 대하여
		if(arrNum==-1)
			System.out.println("\nNOT_FOUND");
			//일치하는 표현을 찾은 경우에 대하여
		else
			output = printFinalOutput(fx, expression, arrNum, lastObject);

		return output;
	}


	/**
	 * 최종 출력
	 * @param fx
	 * @param expression
	 * @param arrNum
	 * @param lastObject
	 * @param foundAdvDesc 부사어는 필요없지만 인터페이스를 맞추기 위하여 둔다
	 */
	private String printFinalOutput(FindExpression fx, String[][] expression, int arrNum, String[] lastObject)
	{
		String output = "NOT_FOUND";
		String a_col = expression[arrNum][0];
		String b_col = expression[arrNum][1];

		if(b_col.startsWith("DYN")||b_col.startsWith("ADV"))					// PickNum에서 이 부분은 필요없지만 통일감을 위하여 둘 뿐이다
			b_col = fx.pasteLastObject_DYNADV(lastObject, b_col);		// '더', '빨리' 등 부사였다면, 앞 명령어의 목적어를 가져온다

		//System.out.println(a_col+"\t"+b_col+foundAdvDesc);			// PickNum에서 부사어는 필요없지만 통일감을 위하여 둘 뿐이다
		output = b_col;

		return output;
	}

}
