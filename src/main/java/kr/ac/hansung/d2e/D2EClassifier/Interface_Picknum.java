package kr.ac.hansung.d2e.D2EClassifier;

import java.util.Scanner;
import com.google.gson.JsonObject;
import linguaFile.FileIO;
import rhino.RHINO;

public class Interface_Picknum 
{
	static boolean findSimilarExpressionWithOrder = false;				//������ ǥ���� ã�� �� ������ ������ ���� ǥ���� ã���� ����
	String[] sheets = null;
	
	public String classsInterface(Scanner sc, JsonObject jsonObject, FileIO fio, FindExpression fx, RHINO rn, MakeQuestion mq, String dicType, String input, String[] ckVxlist, String[][] expression, String[][] expressArr, String[][] expressArr_Original, String[] objectlist, String[] lastObject, String[][] advlist)
	{		
		String output = "NOT_FOUND";
		input = jsonObject.get("input").getAsString();		
		input = fx.splitComplexNounNVxOfInput(dicType, input, fio, ckVxlist);			// �Է¹��� ���Ͽ� ���ո���� ������� ���⸦ �����մϴ� (��, choice ������ ������ �״�� ����)

//		if(input.equals("����")||input.equals("Quit")||input.equals("quit")) {
//			System.out.println("System Quit... Bye");		System.exit(0);		sc.close();
//		}
//		else
		{	
			//�λ�� ���� �κ�
			fx.setFoundFirstAdvNull();													//�տ��� �߰ߵ� �λ�� ������ �ʱ�ȭ�Ѵ�
			fx.setFoundFirstAdvDescNull();											//�տ��� �߰ߵ� �λ�� ���۸��� ������ �ʱ�ȭ�Ѵ�				
			input = fx.findNReplaceADV(input, rn, advlist, "first");				//�λ� ã��, �Է¹������� �����Ѵ�
			@SuppressWarnings("unused")
			String foundAdv = fx.getFoundFirstAdv();								//�߰ߵ� �λ��
			String foundAdvDesc = fx.getFoundFirstAdvDesc();				//�߰ߵ� �λ�� ���۸���				
			
			//arrNum �����κ�. ������ġ�� �ƿ� ������� �ʰ� �Ϸ��� arrNum�� ���� -1�� �ش�
			int arrNum = fx.findResponseExpression_Order_full(input, rn, expressArr, expressArr_Original, lastObject, objectlist);
			lastObject = fx.getFoundFirstObject();						//�� �������� �߰ߵ� ����� �����´�			
			
			//��ġ�ϴ� ǥ���� �� ã�� ��쿡 ���Ͽ�
			if(arrNum==-1) 
				System.out.println("\nNOT_FOUND");
			//��ġ�ϴ� ǥ���� ã�� ��쿡 ���Ͽ�
			else 
				output = printFinalOutput(fx, expression, arrNum, lastObject, foundAdvDesc);			
		}	
		return output;
	}
	
	
	/**
	 * ���� ���
	 * @param fx
	 * @param expression
	 * @param arrNum
	 * @param lastObject
	 * @param foundAdvDesc
	 */
	private String printFinalOutput(FindExpression fx, String[][] expression, int arrNum, String[] lastObject, String foundAdvDesc)
	{
		String output = "NOT_FOUND";
		String a_col = expression[arrNum][0];
		String b_col = expression[arrNum][1];
		
		if(b_col.startsWith("DYN")||b_col.startsWith("ADV"))								// '��', '����' �� �λ翴�ٸ�, �� ���ɾ��� ����� �����´�
			b_col = fx.pasteLastObject_DYNADV(lastObject, b_col);
		
		if(foundAdvDesc==null) {
			System.out.println(a_col+"\t"+b_col);
			output = b_col;
		}
		else {
			System.out.println(a_col+"\t"+b_col+"/"+foundAdvDesc);				//�λ� ���Ǿ��ٸ� �� ������ �����δ�
			output = b_col + "/" + foundAdvDesc;
		}
		return output;
	}

}
