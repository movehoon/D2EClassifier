package kr.ac.hansung.d2e.D2EClassifier;

import java.util.Scanner;
import com.google.gson.JsonObject;
import linguaFile.FileIO;
import rhino.RHINO;

public class Interface_Movements {
	static boolean findSimilarExpressionWithOrder = false;				//������ ǥ���� ã�� �� ������ ������ ���� ǥ���� ã���� ����
	
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
			//ù��° �λ��
			fx.setFoundFirstAdvNull();												//�տ��� �߰ߵ� ù��° �λ�� ������ �ʱ�ȭ�Ѵ�
			fx.setFoundFirstAdvDescNull();										//�տ��� �߰ߵ� ù��° �λ�� ���۸��� ������ �ʱ�ȭ�Ѵ�			
			
			input = fx.findNReplaceADV(input, rn, advlist, "first");					//�λ� ã��, �Է¹������� �����Ѵ�
			@SuppressWarnings("unused")
			String foundFirstAdv = fx.getFoundFirstAdv();					//�߰ߵ� �λ��
			String foundFirstAdvDesc = fx.getFoundFirstAdvDesc();		//�߰ߵ� �λ�� ���۸���		
			
			
			//�ι�° �λ��
			fx.setFoundSecondAdvNull();													//�տ��� �߰ߵ� �ι�° �λ�� ������ �ʱ�ȭ�Ѵ�
			fx.setFoundSecondAdvDescNull();											//�տ��� �߰ߵ� �ι�° �λ�� ���۸��� ������ �ʱ�ȭ�Ѵ�		
			
			input = fx.findNReplaceADV(input, rn, advlist, "second");							//�λ� ã��, �Է¹������� �����Ѵ�
			@SuppressWarnings("unused")
			String foundSecondAdv = fx.getFoundSecondAdv();					//�߰ߵ� �λ��
			String foundSecondAdvDesc = fx.getFoundSecondAdvDesc();		//�߰ߵ� �λ�� ���۸���		
			
			//�� �λ���� ���۸����� ù ��° �λ���� ���۸����� �����Ѵ�
			if(foundFirstAdv!=null && foundSecondAdv!=null)
				foundFirstAdvDesc = foundFirstAdvDesc.concat("/"+foundSecondAdvDesc);
			
			
			//arrNum �����κ�. ������ġ�� �ƿ� ������� �ʰ� �Ϸ��� arrNum�� ���� -1�� �ش�
			boolean ok = this.ckGoodSentence(input, objectlist);		//���� ���������� �׽�Ʈ�Ѵ�.
			if(ok)
			{
				int arrNum = fx.findResponseExpression_Order_full(input, rn, expressArr, expressArr_Original, lastObject, objectlist);					
				lastObject = fx.getFoundFirstObject();							//�� �������� �߰ߵ� ����� �����´�			
				
				//��ġ�ϴ� ǥ���� �� ã�� ��쿡 ���Ͽ�
				if(arrNum==-1) 
				{
					int[][] expressArrPart = null;						
					if(findSimilarExpressionWithOrder) {				//������ ������ ���� ǥ���� ã�� ���(���� �κ��� ���ƾ� ��)
						String[] foundKeyWords = fx.getFoundKeyWords();																											//Order_full ������� �߰ߵ� �ֿ� ���¼�
						expressArrPart = fx.findResponseExpression_Order_Part(input, rn, foundKeyWords, expressArr_Original, lastObject, objectlist);		//�κ������� ��ġ�ϴ� ǥ����
						lastObject = fx.getFoundFirstObject();		//�� �������� �߰ߵ� ����� �����´�
					}
					else {														//������ ������ �޶� �ִ� ǥ���� ��� ã�� ��� (�Ϲ���)
						expressArrPart = fx.findResponseExpression_noOrder(input, rn, expressArr_Original, lastObject, objectlist);
						lastObject = fx.getFoundFirstObject();		//�� �������� �߰ߵ� ����� �����´�
					}						
					
					//�߰ߵ� ��� ����ϱ�
					if(expressArrPart.length==0)
						System.out.println("\nNOT_FOUND");
					else		
					{						
						arrNum = fx.findMostHighObjAdv(expression, expressArrPart, objectlist, advlist);				//�λ���� ��ϰ� �������� ����� �˻��Ͽ� ���� ���� �迭�� �ѹ��� ã�´�
						output = printFinalOutput(fx, expression, expressArrPart[arrNum][0], lastObject, foundFirstAdvDesc);		//expressArrPart[arrNum][0] ��� expressArrPart[0][0]�� ���ϸ� ������ ù ��° ���� �ְ� �ȴ�
					}
				}
				//��ġ�ϴ� ǥ���� ã�� ��쿡 ���Ͽ�
				else 
				{
					output = printFinalOutput(fx, expression, arrNum, lastObject, foundFirstAdvDesc);
				}
			}		
			else
			{
				System.out.println("\nNOT_FOUND/Not-Good-Sentence");
			}
		}	
		return output;
	}
	
	
	/**
	 * ���� ���������� �׽�Ʈ�Ѵ�.
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
	 * �ֿ� ������θ� �̷���� �ʹ� ª�� ������ �ƴ��� �˻��մϴ�. 
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
				System.out.print("\nNOT_FOUND/Not-Enough_Info");			//'��', '��', '������'�� ���� �ֿ� ������θ� �ʹ� ª�� ������ ���
				break;
			}
		}		
		return ok;
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
		
		if(foundAdvDesc!=null)
		{
			b_col = this.changeArm(foundAdvDesc, b_col);
		}
		
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
	
	
	
	/**
	 * ����Ű�� ������ 270�� �̻��̸� �������� ���ȷ� �ٲߴϴ�.
	 * @param foundAdvDesc
	 * @param b_col
	 * @return
	 */
	private String changeArm(String foundAdvDesc, String b_col)
	{
		String[] adv = foundAdvDesc.split("/");
		
		if(b_col.contains("����"))						//���۸����� "������"�� ������ ���� ��
		{
			for(int i=0; i<adv.length; i++)
			{
				if(adv[i].contains("DEG"))
				{
					String[] advdeg = adv[i].split("-");
					String degree = advdeg[1].replace("��", "");
					if(Integer.valueOf(degree) >= 270)
					{
						b_col = b_col.replace("����", "��");
						break;
					}
				}
			}
		}		
		
		return b_col;
	}
	
}
