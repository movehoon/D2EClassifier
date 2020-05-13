package kr.ac.hansung.d2e.D2EClassifier;

import java.util.Scanner;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import linguaFile.FileIO;
import rhino.RHINO;

public class Interface_Choice 
{
	String[] sheets = null;
	
	public String classsInterface(Scanner sc, JsonObject jsonObject, FileIO fio, FindExpression fx, RHINO rn, MakeQuestion mq, String dicType, String input, String[] ckVxlist, String[][] expression, String[][] expressArr, String[][] expressArr_Original, String[] objectlist, String[] lastObject, String[][] advlist)
	{		
		String output = "NOT_FOUND";

		JsonArray jsonArray = jsonObject.get("input").getAsJsonObject().get("choice").getAsJsonArray();
		String[] choice = new String[jsonArray.size()];
		for(int i=0; i<jsonArray.size(); i++) choice[i] = jsonArray.get(i).getAsString(); 
		input = jsonObject.get("input").getAsJsonObject().get("answer").getAsString();	
		
		expression = new String[2][jsonArray.size()+3];											//choice�� ���� expression�� ���� ����
		expression[0][0] = "����"; expression[0][1] = "������ ��ȣ"; expression[0][2] = "����(��ǥ �Ұ�)";
		for(int i=1; i<jsonArray.size()+1; i++) expression[0][i+2] = "����" + String.valueOf(i);
		expression[1][0] = "����"; expression[1][1] = "������ ��ȣ"; expression[1][2] = "����(��ǥ �Ұ�)";
		for(int i=1; i<jsonArray.size()+1; i++) expression[1][i+2] = choice[i-1];
		sheets = mq.makeQuestionNSheets(expression, 1, 2, false);		
		
		input = fx.splitComplexNounNVxOfInput(dicType, input, fio, ckVxlist);			// �Է¹��� ���Ͽ� ���ո���� ������� ���⸦ �����մϴ� (��, choice ������ ������ �״�� ����)

//		if(input.equals("����")||input.equals("Quit")||input.equals("quit")) 
//		{
//			System.out.println("System Quit... Bye");		System.exit(0);		sc.close();
//		}
//		else 
		{	
			String[] result = mq.judgeSelection2(sheets, input, rn);
			output = printFinalOutput(result, sheets);			
		}	
		return output;
	}
	


	/**
	 * ���� ���
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
			judge = "����";
			result = 1;
		}
		else if(found.equals("WRONG")) {
			judge = "����";
			result = -1;
		}
		else if(found.equals("SAME")) {
			judge = "����� ������ ����";
			result = -1;
		}
		else if(found.equals("UNKNOWN")) {
			judge = "�������� ���� �亯";
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
		
		System.out.println("����� ������ "+idx+"��, "+select+"�̸�, "+judge+"�̰�, ������ ����� "+result+"�Դϴ�.\n");
		
		return output;
	}		


}
