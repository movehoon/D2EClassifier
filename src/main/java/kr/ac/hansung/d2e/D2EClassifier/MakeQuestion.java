package kr.ac.hansung.d2e.D2EClassifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import linguaFile.FileIO;
import rhino.RHINO;

public class MakeQuestion {
	FileIO fio = new FileIO();
	
	
	/**
	 * �־��� �������� �ߺ����� �ʴ� ���� ���� �迭�� ����ϴ�. 
	 * @param min ���� �� ���� ���� ��
	 * @param max ���� �� ���� ū ��
	 * @param random ���� �������� ��� ������ ����
	 * @return
	 */
	public int[] makeRandomArr(int min, int max, boolean random) {		
		ArrayList<Integer> list = new ArrayList<>(max+1);
		for (int i = min; i <= max; i++){
		    list.add(i);
		}
		int[] arr = new int[max];		
		if (random)	{				//�������� ���� �ִ� ��� 
			for (int count = 0; count < max; count++){
			    arr[count] = list.remove((int)(Math.random() * list.size()));
			}
		}
		else {							//������� ���� �ִ� ���
			for (int count = 0; count < max; count++){
			    arr[count] = list.get(count);
			}
		}		
		return arr;
	}
	
	
	/**
	 * ������ �������� ����Ѵ�
	 * @param expression �м��� ���� ����
	 * @param questNum ���õ� ������ ��ȣ
	 * @param questCol ������ �ִ� �÷��� ��ȣ
	 * @return
	 */
	public String[] makeQuestionNSheets(String[][] expression, int questNum, int questCol, boolean print)
	{		
		int selectColAmount = (expression[questNum].length -1) - questCol;
		String quest = expression[questNum][questCol];							// ���� ����
		
		String[] sheets = new String[selectColAmount+3];						// �������� �޴� �迭. ��ȣ�� ���߱� ���� ó���� ����ΰ�, �������� �����ȣ�� ������ ��´�		
		int start = questCol+1;
		int end = questCol+selectColAmount;
		
		for (int i=start, j=1; i<=end; i++, j++) {										// �������� ������ �ϳ��� �迭�� ��´�
			sheets[j] = expression[questNum][i].trim();
		}	
		
		if(print)
			System.out.println(quest);														// ���� ���
		for (int i=1; i<selectColAmount+1; i++) {									// ������ ���
			String str = i + "��, " + sheets[i];
			if(print)
				System.out.println(str);
		}
		
		sheets[selectColAmount+1] = expression[questNum][0].trim();		// ����
		sheets[selectColAmount+2] = expression[questNum][1].trim();		// �����ȣ
		
		return sheets;
	}
	
	

	/**
	 * ������ �Է��ߴ��� ���� �Ǵ��մϴ�.
	 * @param sheets
	 * @param input
	 * @param rn
	 * @return
	 */
	public String judgeSelection(String[] sheets, String input, RHINO rn)
	{
		String found = "NULL";
		
		String[] cklist = {"NNG", "NNP", "NR", "SN", "MM"};							//������� ���ڸ� �غ��صд�. "ù, ��, ��, ��"�� MM�̴�.
		String[] inputArr = rn.GetOutputPartArr_cklist(rn.ExternCall(input), cklist);
		
		// ������ ��İ� �����ϰ� �Է��� ��������� �˻��Ѵ� (��: ��Ÿ�� / 1 / 1��)
		if(found.equals("NULL"))
			found = this.judgeSelection_Same(sheets, input);
		
		// ������ �Է¹����μ�, ���¼� �м� �� ���� �ϳ��� ����� ������ ��İ� �������� �˻��Ѵ� (��: ��Ÿ�Ϳ� / 1�̿� / 1���̿� / �Ϲ��̿�)
		if(found.equals("NULL"))
			found = this.judgeSelection_MorphSame(sheets, inputArr);
		
		// ��ī�� ���絵�� ó��
		if(found.equals("NULL"))
			found = this.judgeSelection_Jaccard(sheets, inputArr);
		
		return found;
	}	
		
	/**
	 * �Է¹��� ���� �Ǵ� �������� �����ϰ�(equals) �ۼ��Ǿ������� Ȯ���մϴ�
	 * '��Ÿ��'ó�� ���ڷ� ��ġ�ϰų�, '1' �Ǵ� '1��'���� ���ڷ� ��ġ�ϴ� ��츦 ã���ϴ�.
	 * @param sheets
	 * @param input
	 * @return
	 */
	private String judgeSelection_Same(String[] sheets, String input)
	{
		String found = "NULL";		
		input= changeStringToNum(input);						// ������ ���ڸ� ������ ���ڷ� �ٲ��ش�
		
		if(input.length()==2 && input.endsWith("��"))			// "1�� --> 1" �� �����Ѵ�.
			input = input.replace("��", "");		
		
		if(sheets[sheets.length-2].equals(input)) {				// ���ڿ��� �˻� (��: ������)
			found = "RIGHT";
		}
		else if(sheets[sheets.length-1].equals(input))	{		// ��ȣ�� �˻� (��: 2)
			found = "RIGHT";
		}
		
		if(found.equals("NULL"))										// ���� ���� �߰ߵ��� �ʾҴٸ�
		{
			for(int i=1; i<sheets.length-2; i++) {
				if(sheets[i].equals(input)) {							// �Է¹��� 1~4�� �������� �ִٸ�
					found = "WRONG";
					break;
				}
			}
		}
		
		if(found.equals("NULL"))										// ���� ���� �߰ߵ��� �ʾҴٸ�
		{
			for(int i=1; i<sheets.length-2; i++) {
				if(Integer.toString(i).equals(input)) {				// �� ��ȣ�� 1~4 ���̿� �ִٸ�
					found = "WRONG";
					break;
				}
			}
		}
		
		return found;
	}
	
	
	/**
	 * ������ ���ڸ� ������ ���ڷ� �ٲ��ش�
	 * ���� ��ġ �˻�
	 * @param input
	 * @return
	 */
	private String changeStringToNum(String input)
	{
		if(input.equals("��")||input.equals("�Ϲ�")||input.equals("�ϳ�")||input.equals("ù")||input.equals("ó��")||input.equals("ù°"))
			input = "1";
		else if(input.equals("��")||input.equals("�̹�")||input.equals("��")||input.equals("��")||input.equals("��°")||input.equals("��°"))
			input = "2";
		else if(input.equals("��")||input.equals("���")||input.equals("��")||input.equals("��")||input.equals("��°")||input.equals("��°"))
			input = "3";
		else if(input.equals("��")||input.equals("���")||input.equals("��")||input.equals("��")||input.equals("��°")||input.equals("��°"))
			input = "4";
		else if(input.equals("��")||input.equals("����")||input.equals("�ټ�")||input.equals("�ټ�°"))
			input = "5";
		else if(input.equals("��")||input.equals("����")||input.equals("����")||input.equals("����°"))
			input = "6";
		else if(input.equals("ĥ")||input.equals("ĥ��")||input.equals("�ϰ�")||input.equals("�ϰ�°"))
			input = "7";
		else if(input.equals("��")||input.equals("�ȹ�")||input.equals("����")||input.equals("����°"))
			input = "8";
		else if(input.equals("��")||input.equals("����")||input.equals("��ȩ")||input.equals("��ȩ°"))
			input = "9";
		else if(input.equals("��")||input.equals("�ʹ�")||input.equals("��")||input.equals("��°"))
			input = "10";		
		
		return input;
	}
	
		
	/**
	 * ������ ǥ�� �ӿ� �־� ���¼� �м� ����� �����ϴ� ����Դϴ�.
	 * �׷��� ���¼Ұ� �� �ϳ��� �ִ� ��츦 ������� �մϴ�.
	 * ���¼Ұ� �� �̻� ������ ��ī�� ���絵�� ó���ؾ� �մϴ�.
	 * @param sheets
	 * @param inputArr
	 * @return
	 */
	private String judgeSelection_MorphSame(String[] sheets, String[] inputArr)
	{
		String found = "NULL";
		
		if(inputArr.length==1)														//�ش� ��Ұ� �ϳ��� ��쿡 ���ؼ��� �����Ѵ�
			found = this.judgeSelection_Same(sheets, inputArr[0]);
		
		return found;
	}	
	
	
	/**
	 * ������� ���ڰ� �� �̻� �־ ��ī�� ���絵 ������� ó���ϴ� ����Դϴ�.
	 * @param sheets
	 * @param inputArr
	 * @return
	 */
	private String judgeSelection_Jaccard(String[] sheets, String[] inputArr)
	{
		String found = "NULL";
		int rightCnt = 0;
		int wrongCnt = 0;
		
		for(int i=0; i<inputArr.length; i++)										//�ش� ��Ұ� �� �̻��� ��쿡 ���ؼ� �����Ѵ�
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
	 * ������ ���� ������� �Է¹��� �������� � �Ͱ� ���� ���� ��ġ�ϴ����� ����
	 * @param sheets
	 * @param input
	 * @param rn
	 * @return
	 */
	public String[] judgeSelection2(String[] sheets, String input, RHINO rn)
	{
		String[] result = {"NULL", "NULL", "NULL"};		
		String[] cklist = {"NNG", "NNP", "NR", "SN", "MM"};							//������� ���ڸ� �غ��صд�. "ù, ��, ��, ��"�� MM�̴�.
		String[] inputArr = rn.GetOutputPartArr_cklist(rn.ExternCall(input), cklist);
		
		//String found = "NULL";
		// ������ ��İ� �����ϰ� �Է��� ��������� �˻��Ѵ� (��: ��Ÿ�� / 1 / 1��)		
		//if(found.equals("NULL"))
		//	result = this.judgeSelection_Same2_outer(sheets, input);		
		// �ణ ������ �Է¹����μ�, �ϳ��� ����� ������, ���¼� �м� �� ���� �ϳ��� ����� ������ ��İ� �������� �˻��Ѵ� (��: ��Ÿ�Ϳ� / 1�̿� / 1���̿� / �Ϲ��̿�)
		//if(result[0].equals("NULL"))
		//	result = this.judgeSelection_MorphSame2(sheets, inputArr);
		
		// ���� ������ �Է¹����μ�, �������� ����� ������, ��ī�� ���絵�� ó��. (��: ������ ��Ÿ�� ���ƿ� / ������ ��Ÿ�� ���⵵ �ϰ� ������ ���⵵ �ѵ�, 1������ �ҷ�)
		// �ᱹ �� �Լ� �ϳ��� ��� ��츦 ó���� �� �ִ�
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
	 * �Է¹��� �������� �����ϰ�(equals) �ۼ��Ǿ������� Ȯ���մϴ�
	 * '��Ÿ��'ó�� ���ڷ� ��ġ�ϰų�, '1' �Ǵ� '1��'���� ���ڷ� ��ġ�ϴ� ��츦 ã���ϴ�.
	 * �Է¹��� �������� �����ϱ⸸ �ϸ� ������ �´� ������ �Ǵ��մϴ�.
	 * @param sheets
	 * @param input
	 * @return
	 */
	private String[] judgeSelection_Same2_inner(String[] sheets, String input)
	{
		String[] result = {"NULL", "NULL", "NULL"};
		String found = "NULL";		
		String select = "NULL";
		input= this.changeStringToNum(input);						// ������ ���ڸ� ������ ���ڷ� �ٲ��ش�
		
		if(input.length()==2 && input.endsWith("��"))			// "1�� --> 1" �� �����Ѵ�.
			input = input.replace("��", "");		
		
		if(found.equals("NULL"))										// ���� ���� �߰ߵ��� �ʾҴٸ�
		{
			for(int i=1; i<sheets.length-2; i++) {
				if(sheets[i].equals(input)) {							// �Է¹��� �������� �ִٸ� ������ ����
					found = "RIGHT";
					select = String.valueOf(i-1);
					break;
				}
			}
		}
		
		if(found.equals("NULL"))									// ���� ���� �߰ߵ��� �ʾҴٸ�
		{
			for(int i=1; i<sheets.length-2; i++) {
				if(Integer.toString(i).equals(input)) {				// �� ��ȣ�� ������ ������ �ִٸ� ������ ����
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
	 * ������� �亯 ��� �� ��� ����(1, 2, 3, 4)�� �Ǿ� �ִ� ���� ����� ���ڷ� ��ȯ�մϴ�.
	 * @param sheets
	 * @param inputArr
	 * @return
	 */
	private String[] intToStrAnswer(String[] sheets, String[] inputArr)
	{
		String regExp = "^[0-9]+$";								//���� �ν��� ���� ���Խ�
		
		for(int i=0; i<inputArr.length; i++)
		{
			if(inputArr[i].matches(regExp))
			{
				int strNum = Integer.valueOf(inputArr[i]);
				if(strNum <= sheets.length-3)					// �� ���ڰ� ���������� ū ���� �ݿ����� �ʴ´�
					inputArr[i] = sheets[strNum];					// �������� �ش� ��ȣ�� ��ü�Ѵ� 
			}
		}		
		
		return inputArr;
	}
	
	
	/**
	 * ������ ǥ�� �ӿ� �־� ���¼� �м� ����� �����ϴ� ����Դϴ�.
	 * �׷��� ���¼Ұ� �� �ϳ��� �ִ� ��츦 ������� �մϴ�.
	 * ���¼Ұ� �� �̻� ������ ��ī�� ���絵�� ó���ؾ� �մϴ�.
	 * @param sheets
	 * @param inputArr
	 * @return
	 */
	@SuppressWarnings("unused")
	private String[] judgeSelection_MorphSame2(String[] sheets, String[] inputArr)
	{
		String[] result = {"NULL", "NULL", "NULL"};
		
		if(inputArr.length==1)														//�ش� ��Ұ� �ϳ��� ��쿡 ���ؼ��� �����Ѵ�
			result = this.judgeSelection_Same2_inner(sheets, inputArr[0]);
		
		return result;
	}	
	
	
	/**
	 * ������� �亯 ��� �� ���� ����(ù, ��, ��, ��)�� �Ǿ� �ִ� ���� ����� ���ڷ� ��ȯ�մϴ�.
	 * @param sheets
	 * @param inputArr
	 * @return
	 */
	private String[] ordinalToCardinal(String[] sheets, String[] inputArr)
	{
		boolean found = false;
		String regExp = "^[0-9]+$";								//���� �ν��� ���� ���Խ�
		String strNumCandidate = "";
		
		//{"method":"choice", "input":{"choice": ["��° �Ƶ�","ù° �Ƶ�","��° �Ƶ�","���� ����"], "answer": "��° �Ƶ��̿�"}}  �� ���� �κ�
		for(int i=0; i<inputArr.length-1; i++)
		{
			for(int j=1; j<sheets.length-2; j++)
			{
				if(sheets[j].startsWith(inputArr[i]))
				{
					if(sheets[j].contains(inputArr[i+1]))
					{
						inputArr[i] = sheets[j];					// �������� �ش� ��ȣ�� ��ü�Ѵ� 
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
				strNumCandidate = changeStringToNum(inputArr[i]);							//��ġ �˻����� �ذ�			
				if(strNumCandidate.matches(regExp))
				{
					int strNum = Integer.valueOf(strNumCandidate);
					if(strNum <= sheets.length-3)					// �� ���ڰ� ���������� ū ���� �ݿ����� �ʴ´�
						inputArr[i] = sheets[strNum];					// �������� �ش� ��ȣ�� ��ü�Ѵ� 
				}
			}		
		}
		
		return inputArr;
	}
	
	
	/**
	 * ���� ã�� ���� �ܾ ����Ѵ�
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
				if(rightWord[j][0].equals(inputArr[i]))		{				//�̹� �ִ� ���, ī��Ʈ �ø� 				
					isthere = true;
					int temp = Integer.valueOf(rightWord[j][1]);
					rightWord[j][1] = String.valueOf(temp+1);
					break;
				}
			}
			
			if(!isthere)	{														//���� ���� ��� ���� ���			
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
	 * ���� ã�� ���� �ܾ ���������� ��ȯ�Ѵ�
	 * @param rightWord
	 * @param rightWord2
	 * @return
	 */
	private int[][] enrollRightWord2(String[][] rightWord, int[][] rightWord2)
	{
		//int �迭�� Copy
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
		
		//�������� ����
		Arrays.sort(rightWord2, new Comparator<int[]>() {      
	        @Override
	        public int compare(int[] o1, int[] o2) {
	            return Integer.compare(o2[1], o1[1]);
	        }
	    });			
		
		return rightWord2;
	}
	
	
	/**
	 * null�� ������ �������� �迭�� ���̸� ã�´�
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
				arrayInt = i;											//0 ���� ���Ÿ� ���ؼ��� ���ʿ��ϰ� ������ 1�� �ٽ� ����� �Ѵ�
				break;
			}
		}	
		
		if(rightWord2.length==1 && arrayInt==0)		//Ư�� ����
			arrayInt = 1;
		
		return arrayInt;
	}
	
	
	/**
	 * ��� �迭 result�� ����ϴ�.
	 * @param sheets
	 * @param rightWord
	 * @param rightWord2
	 * @return
	 */
	private String[] makeResult(String[] sheets, String[][] rightWord, int[][] rightWord2)
	{
		String[] result = {"NULL", "NULL", "NULL"};
		int arrayInt = findArrayInt(rightWord, rightWord2);					//0 ���Ҹ� �����ϱ�						
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
			if(rightWord3[0][1]==rightWord3[1][1]) {			//�ּ��� �� ���� ������ �󵵸� ���´ٸ� �װ��� ȥ�������� �����̴�
				result[0] = "SAME";
				result[1] = "NULL";
				result[2] = "NULL";
			}
			else {
				int selectIdx = rightWord3[0][0];					//���� �󵵰� ���� ������ �ε���
				String select = rightWord[selectIdx][0];			
				result[0] = "RIGHT";
				result[1] = select;
				result[2] = this.findIndexOfWord(sheets, select);
			}
		}		
		else {
			int selectIdx = rightWord3[0][0];						//���� �󵵰� ���� ������ �ε���
			String select = rightWord[selectIdx][0];			
			result[0] = "RIGHT";
			result[1] = select;
			result[2] = this.findIndexOfWord(sheets, select);
		}		
		
		return result;
	}
	
	
	/**
	 * ������� ���ڰ� �� �̻� �־ ��ī�� ���絵 ������� ó���ϴ� ����Դϴ�.
	 * @param sheets
	 * @param inputArr
	 * @return
	 */
	private String[] judgeSelection_Jaccard2(String[] sheets, String[] inputArr)
	{	
		inputArr = this.intToStrAnswer(sheets, inputArr);					//������ �亯�� �������� ���ڷ� ġȯ�Ѵ�
		inputArr = this.ordinalToCardinal(sheets, inputArr);					//����(ù, ��, ��, ��)�� ���(1, 2, 3, 4)�� ġȯ�Ѵ�
				
		String[][] rightWord = new String[inputArr.length][2];		
		rightWord = enrollRightWord(sheets, inputArr, rightWord);		//���� ã�� ���� �ܾ ����Ѵ�
		
		int[][] rightWord2 = new int[inputArr.length][2];		
		rightWord2 = enrollRightWord2(rightWord, rightWord2);		//���� ã�� ���� �ܾ ���������� ��ȯ�Ѵ�		
		
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
