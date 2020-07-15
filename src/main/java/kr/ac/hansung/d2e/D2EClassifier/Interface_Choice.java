package kr.ac.hansung.d2e.D2EClassifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import linguaFile.FileIO;
import rhino.RHINO;

public class Interface_Choice
{
	String[] sheets = null;

	public String classsInterface(JsonObject jsonObject, FileIO fio, PreProcessor pp, FindExpression fx, RHINO rn, MakeQuestion mq, String dicType, String[] cklist, String[] ckVxlist, String[][] expression, String[][] expressArr, String[][] expressArr_Original, String[] objectlist, String[] lastObject, String[][] advlist)
	{
		String output = "NOT_FOUND";

		JsonArray jsonArray = jsonObject.get("input").getAsJsonObject().get("choice").getAsJsonArray();
		String[] choice = new String[jsonArray.size()];
		for(int i=0; i<jsonArray.size(); i++) choice[i] = jsonArray.get(i).getAsString();
		String input = jsonObject.get("input").getAsJsonObject().get("answer").getAsString();

		expression = new String[2][jsonArray.size()+3];											//choice를 위해 expression을 새로 구성
		expression[0][0] = "정답"; expression[0][1] = "정답의 번호"; expression[0][2] = "질문(쉼표 불가)";
		for(int i=1; i<jsonArray.size()+1; i++) expression[0][i+2] = "선택" + String.valueOf(i);
		expression[1][0] = "정답"; expression[1][1] = "정답의 번호"; expression[1][2] = "질문(쉼표 불가)";
		for(int i=1; i<jsonArray.size()+1; i++) expression[1][i+2] = choice[i-1];
		sheets = mq.makeQuestionNSheets(expression, 1, 2, false);

		input = pp.splitComplexNounNVxOfInput(rn, dicType, input, fio, ckVxlist);		// 입력문에 대하여 복합명사와 보조용언 띄어쓰기를 수행합니다 (단, choice 블럭은 원문을 그대로 리턴)
		input = pp.GetInputArr_cklist(input, rn.ExternCall(input, true), cklist);				// input에서 cklist가 있는 어절만 모아 다시 String으로 만든다 (이 줄은 삭제해도 된다)
		//형태소 분석은 여기서 하지 않고 MakeQuestion 클래스에서 진행한다 (input, inputArr 두 가지를 모두 받을 수 있도록)

		String[] result = mq.judgeSelection2(sheets, input, rn, cklist);
		output = printFinalOutput(result, sheets);

		return output;
	}



	/**
	 * 최종 출력
	 * @param resultArr
	 * @param sheets
	 * @return
	 */
	private String printFinalOutput(String[] resultArr, String[] sheets)
	{
		String output = "NOT_FOUND";

		String found = resultArr[0];
		String select = resultArr[1];
		String idx = resultArr[2];

		String judge = "";
		int result = -1;

		if(found.equals("RIGHT")) {
			judge = "확정적";
			result = 1;
		}
		else if(found.equals("WRONG")) {			//현재 WRONG은 없다
			judge = "오답(현재는 이것이 나올 수 없다)";
			result = -1;
		}
		else if(found.equals("SAME")) {
			judge = "정답과 오답이 동수";
			result = -1;
		}
		else if(found.equals("UNKNOWN")) {
			judge = "선택지에 없는 답변";
			result = -1;
		}

		if (resultArr[2].compareToIgnoreCase("null")==0) {
			output = "-1";
		}
		else if (resultArr[2].length() > 0) {
			output = resultArr[2];
		}
		else {
			output = resultArr[1];
		}

		System.out.println("당신의 선택은 "+idx+"번, "+select+"이며, "+judge+"이고, 숫자형 결과는 "+result+"입니다.\n");

		if (select == null)
			select = "";
		return select;
	}


}
