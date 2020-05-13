package kr.ac.hansung.d2e.D2EClassifier;

import java.util.ArrayList;
import java.util.List;

import linguaFile.FileIO;
import rhino.RHINO;

public class FindExpression 
{
	private String[] cklist; 	
	private String[][] expressArr;									//stem ����� ���� �� ���� 
	private String[][] expressArr_original;						//stem ����� ���� �� ���� 	
	private String[] foundKeyWords;							//Ž�� ���� �� ã�� ������ ���ֵ�
	private String[] foundFirstObject;							//ù��°�� �߰ߵ� ������ ����
	private String foundFirstAdv;									//ù��°�� �߰ߵ� �λ�� ����
	private String foundFirstAdvDesc;							//ù��°�� �߰ߵ� �λ�� ���� ���۸���
	private String foundSecondAdv;							//�ι�°�� �߰ߵ� �λ�� ����
	private String foundSecondAdvDesc;						//�ι�°�� �߰ߵ� �λ�� ���� ���۸���
	
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
	
	public String getFoundFirstAdv() {
		return this.foundFirstAdv;
	}
	
	public String getFoundFirstAdvDesc() {
		return this.foundFirstAdvDesc;
	}
	
	public String getFoundSecondAdv() {
		return this.foundSecondAdv;
	}
	
	public String getFoundSecondAdvDesc() {
		return this.foundSecondAdvDesc;
	}
	
	public void setFoundFirstAdvNull() {
		this.foundFirstAdv = null;
	}
	
	public void setFoundFirstAdvDescNull() {
		this.foundFirstAdvDesc = null;
	}
	
	public void setFoundSecondAdvNull() {
		this.foundSecondAdv = null;
	}
	
	public void setFoundSecondAdvDescNull() {
		this.foundSecondAdvDesc = null;
	}
	
	
	/**
	 * �������� ��ٰ� �������¼�(��,����...)���� �����Ͽ� �����ϰ�, ���� �� ������ ����ϴ�.
	 * @param rn ���¼Һм��� ��ü
	 * @param fio FileIO ��ü
	 * @param expression �� ����
	 */
	public void sortExpressionDictionaryAndCopy(RHINO rn, FileIO fio, String[][] expression)
	{
		//������ ������ ���¼� �м��� ��, stem �κи� �迭�� ���� ��ü�� 2���� �迭�� �����ϱ� 
		String[][] expressArr = new String[expression.length][];												//�ٱ� �迭(1����)�� ũ�⸸ �����ش�
		String[][] expressArr_original = new String[expression.length][];									//���� �� ����� �����ϴ� �迭�� �����Ѵ�.
		
		GetNeededMorph gm = new GetNeededMorph();
		
		for(int i=0; i<expressArr.length; i++)
		{
			String[] morph = gm.GetOutputPartArr_cklist(rn.ExternCall(expression[i][0]), cklist);			
			expressArr[i] = new String[morph.length];																//���� �迭(2����)�� ũ�⸦ �׶����� �����ش�.
			expressArr_original[i] = new String[morph.length+1];
			
			int col = 0;
			for(int j=0; j<expressArr[i].length; j++)
			{
				expressArr[i][j] = morph[j];
				expressArr_original[i][j] = morph[j];
				col++;
			}
			expressArr_original[i][col] = String.valueOf(i);			//���� �� ����� �����ϴ� �迭�� ������ row(outer) ��ȣ�� �������� �����Ѵ�.
		}		
		
		gm.sort2DArrayByLength(expressArr, true);					//2���� �迭�� �� ������ ����� ���� �����Ѵ�.
		
		// �Ʒ� 7���� ���� ���� �������� empty�� �� �迭�� �������ش�
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
	 * �Է¹��� stem ������ ���뿡�� ���� �������� ���Ͽ� ���� ���� �� ������ �迭 ��ȣ�� ã���ݴϴ�.
	 * @param inputSentence �Է¹�
	 * @param rn ���¼Һм��� ��ü
	 * @param expressArr stem���� �� ����
	 * @param expressArr_original stem���� �� ����
	 * @param objectlist ������ ����Ʈ
	 * @return
	 */
	public int findResponseExpression_Order_full(String inputSentence, RHINO rn, String[][] expressArr, String[][] expressArr_original, String[] lastObject, String[] objectlist)
	{
		GetNeededMorph gm = new GetNeededMorph();
		int arrNum = -1;			
		String[] inputArr = gm.GetOutputPartArr_cklist(rn.ExternCall(inputSentence), cklist);		
		
		String[] foundKeyWords = new String[inputArr.length];
		int kw = 0;
				
		boolean found = false;			
		for(int i=0; i<expressArr.length; i++)								//��� ǥ���� �ϳ��� �����Ѵ�.
		{
			for(int j=0; j<inputArr.length; j++)							//������ ��� �κп������� ��ġ���� �� �� ����.
			{				
				try {				
					if(inputArr[j].equals(expressArr[i][0]))						//ǥ���� ù ���ۺκа� ���ƾ� �Ѵ�.
					{
						if(ckNotIncludedYet(foundKeyWords, expressArr[i][0])) 	//���� �Էµ��� ���� ���ְ� �´ٸ�, 
						{
							foundKeyWords[kw] = expressArr[i][0];					//�߰��� ���� �����Ѵ� 			
							kw++;
						}					
						
						int cntExpressSame = 1;											//ǥ���� ���ڿ� ���� ���� ���� �Ǵ����� ī��Ʈ
						if(cntExpressSame==expressArr[i].length)
						{
							arrNum = findOriginalArray(expressArr, expressArr_original, i);
							found = true;
							break;
						}
						else if(inputArr.length-1==j)									//���� ������ �Է¹��� �������ε�, ������ �ɸ��� �ʾҴٸ�, ���� ǥ������ ���� �Ѵ�.
						{
							break;
						}
						else
						{
							j++;
							for(int k=1; k<expressArr[i].length && j<inputArr.length; k++, j++)		//������ ���� �� �Է¹��� ���̺��� ���� �������� ����
							{
								if(inputArr[j].equals(expressArr[i][k]))
								{
									if(ckNotIncludedYet(foundKeyWords, expressArr[i][k])) 				//���� �Էµ��� ���� ���ְ� �´ٸ�, 
									{
										foundKeyWords[kw] = expressArr[i][k];								//�߰��� ���� �����Ѵ� 			
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
					// ������ �籸���ϸ鼭 empty �迭�� ����� �ȴ�. �̰��� �����ϴ� ���� ������ �߻��ϹǷ� try-catch �� ó���Ѵ�
					arrNum = -1;
				}
			}
			if(found) 
				break;			
		}
		
		this.ckHaveObject(inputArr, objectlist);											//�߰ߵ� ù��° ����� Ŭ���� ������ ��� �Ѵ�.	
		this.foundKeyWords = gm.removeNullValue(foundKeyWords);			//null ���Ұ� �ִ� �κк��ʹ� �����Ͽ� ��������.
		return arrNum;
	}		
	
	
	/**
	 * �Է¹��� �ٽ� ���ָ� �κ������� �����ϴ� ������ ǥ������ ã���ݴϴ�. (������ �����ؾ� ��)
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
		String[] inputArr = gm.GetOutputPartArr_cklist(rn.ExternCall(inputSentence), cklist);		
		int[][] expressArrOriginalCnt = new int[expressArr_Original.length][2];			//�� ǥ������ ��ġ�ϴ� Ű������ �� 
		
		boolean hasObject = this.ckHaveObject(inputArr, objectlist);						//����� ������ �ִ��� Ȯ���Ѵ�
		if(!hasObject)																					//������ ���� �ʴٸ�,
			inputArr = this.includeObject(inputArr, lastObject);								//������ ����� �տ� ä�� �ִ´�.		
		
		for(int i=0; i<expressArr_Original.length; i++)											//��� ǥ���� �ϳ��� �����Ѵ�
		{
			for(int j=0; j<foundKeyWords.length; j++)											//�߰ߵ� Ű���� ���ָ� �� �����Ѵ� 
			{
				int expressArr2DLen = expressArr_Original[i].length;							//���� ������ ���� �ִ� ǥ���� ��
				for(int k=0; k<expressArr2DLen; k++)											//���� ǥ���� ��� ���ֿ� ���Ͽ� �����Ѵ� 
				{
					if(foundKeyWords[j].equals(expressArr_Original[i][k]))
					{
						expressArrOriginalCnt[i][0] = i;												//������ ���� �迭 ��ȣ
						expressArrOriginalCnt[i][1] += 1;											//�߰��� Ƚ���� �����Ѵ�
						break;
					}
				}
			}
		}		
		
		this.ckHaveObject(inputArr, objectlist);													//�߰ߵ� ù��° ����� Ŭ���� ������ ��� �Ѵ�.		
		return gm.remove0Value2D(this.sort2DArr(gm, expressArrOriginalCnt), 1);		//�����Ұ� ���� ������ �������� �����Ͽ� ��������;					
	}
			
	
	/**
	 * ������ ������� �Է¹��� ������ ��ġ�ϴ� ǥ���� ã�´�
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
		String[] inputArr = gm.GetOutputPartArr_cklist(rn.ExternCall(inputSentence), cklist);		
		int[][] expressArrOriginalCnt = new int[expressArr_Original.length][2];
		
		boolean hasObject = this.ckHaveObject(inputArr, objectlist);					//����� ������ �ִ��� Ȯ���Ѵ�
		if(!hasObject)																					//������ ���� �ʴٸ�,
			inputArr = this.includeObject(inputArr, lastObject);								//������ ����� �տ� ä�� �ִ´�.		
		
		for(int i=0; i<expressArr_Original.length; i++)										//��� ǥ���� �ϳ��� �����Ѵ�.
		{			
			for(int j=0; j<inputArr.length; j++)													//������ ��� �κп������� ��ġ���� �� �� ����.
			{
				int expressArr2DLen = expressArr_Original[i].length;						//���� ������ ���� �ִ� ǥ���� ��				
				for(int k=0; k<expressArr2DLen; k++)
				{						
					if(inputArr[j].equals(expressArr_Original[i][k]))								//!!!���⼭ ���� ���� �˻��Ͽ� �����Ѵ�!!!
					{
						expressArrOriginalCnt[i][0] = i;											//������ ���� �迭 ��ȣ
						expressArrOriginalCnt[i][1] += 1;											//�߰��� Ƚ���� �����Ѵ�
						break;
					}
				}
			}
		}
		
		this.ckHaveObject(inputArr, objectlist);													//�߰ߵ� ù��° ����� Ŭ���� ������ ��� �Ѵ�.		
		int[][] testArr = this.sort2DArr(gm, expressArrOriginalCnt);
		int[][] testArr_ordered = gm.remove0Value2D(testArr, 1);						//�����Ұ� ���� ������ �������� �����Ͽ� ��������
		int[][] testArr_high = gm.getHighNumber2D(testArr_ordered, 1, 2);			//�����Ұ� ���� ���� �׷�鸸 �߷�����
		return testArr_high;												
	}
	
	
	/**
	 * �������� �λ��(ADV)�� ����� �����´�
	 * @param movements_text
	 * @return
	 */
	public String[][] getAdvList(String movements_text)
	{
		String[] movements_arr = movements_text.split("\r\n");
		
		int advcnt = 0;
		for(int i=0; i<movements_arr.length; i++)
		{
			String[] temp = movements_arr[i].split(",");			
			if(temp[1].startsWith("ADV"))
				advcnt++;																//�켱 �������� ADV�� ���� �ľ��Ѵ� 
		}
		
		String[][] advlist = new String[advcnt][2];
		int cnt = 0;
		for(int i=0; i<movements_arr.length; i++)
		{
			String[] temp = movements_arr[i].split(",");			
			if(temp[1].startsWith("ADV"))
			{
				if(temp[0].endsWith("��"))
					temp[0] = temp[0].substring(0, temp[0].length()-1);	//���ڰ� --> ����, ������ --> ����
				
				advlist[cnt][0] = temp[0];											// ���
				advlist[cnt][1] = temp[1];											// ���۸���
				cnt++;
			}
		}
		
		return advlist;
	}
		
	
	/**
	 * �Է¹��� �λ� �ִ����� Ȯ���ϰ�, ������ �λ��� ������ �� �Է¹����� �ش� �λ� �����Ѵ�
	 * @param inputSentence
	 * @param rn
	 * @param advlist
	 * @param times "first" �Ǵ� "second"
	 * @return
	 */
	public String findNReplaceADV(String inputSentence, RHINO rn, String[][] advlist, String times)
	{
		boolean found = false;		
		GetNeededMorph gm = new GetNeededMorph();
		String[] inputArr = gm.GetOutputPartArr_cklist(rn.ExternCall(inputSentence), cklist);
		
		String[] durationExp = null;																//���� �ð� ǥ��
		String duration = "";
		String durationUnit = "";
		
		String[] directionExp = null;																//���� ǥ��
		String direction = "";
		String directionUnit = "";
		
		if(inputArr.length > 1)																		//�λ� Ȧ�� �ִ� ���� ���ܵȴ�
		{
			durationExp = this.findDurationTime(inputSentence, inputArr);				//�����ð�ǥ���� �־��ٸ� �ð��� ���, �ش�ǥ���� ������ �Է¹��� �޾ƿ´�
			duration = durationExp[0];																//�����ð�(����)
			durationUnit = durationExp[1];														//�����ð�����(��,��,�ð�)
			inputSentence = durationExp[2];														//�����ð�ǥ���� ������ �Է¹�
			inputArr = gm.GetOutputPartArr_cklist(rn.ExternCall(inputSentence), cklist);		//������ ǥ������ �ٽ� ���¼� �м� 
			
			directionExp = this.findDirection(inputSentence, inputArr);					//����ǥ���� �־��ٸ� ������ ���, �ش�ǥ���� ������ �Է¹��� �޾ƿ´�
			direction = directionExp[0];															//����(���� ����)
			directionUnit = directionExp[1];														//����ǥ������(����, ��, ��)
			inputSentence = directionExp[2];													//����ǥ���� ������ �Է¹�
			inputArr = gm.GetOutputPartArr_cklist(rn.ExternCall(inputSentence), cklist);		//������ ǥ������ �ٽ� ���¼� �м� 
			
			//�Ϲ� �λ�� ã��
			for(int i=0; i<advlist.length; i++)
			{
				for(int j=0; j<inputArr.length; j++)
				{
					if(advlist[i][0].startsWith(inputArr[j]))
					{
						if(inputArr[j].equals(advlist[i][0]))
						{
							found = true;
							if(times.contains("first"))
							{
								this.foundFirstAdv = advlist[i][0];
								this.foundFirstAdvDesc = advlist[i][1];
							}
							else if(times.contains("second"))
							{
								this.foundSecondAdv = advlist[i][0];
								this.foundSecondAdvDesc = advlist[i][1];
							}							
							
							inputSentence = inputSentence.replace(advlist[i][0], "");			//�տ��� ��ó��. �迭 ��ȯ �� �λ� equals�� ã�� ��, �ٽ� ��ġ��. ã�� �λ��� �Է¹������� �����Ѵ�
							inputSentence = inputSentence.replace(" �� ", " ");				//�ӽ� ó��
							inputSentence = inputSentence.replace(" ���� ", " ");				//�ӽ� ó��
							inputSentence = inputSentence.replace(" ��ŭ ", " ");				//�ӽ� ó��
							break;
						}
						else					//"Ƽ �ȳ���"�� ���� ���� ���� �λ���� ���
						{
							String tempAdv = advlist[i][0].replaceAll(" ", "");
							String tempStr = inputArr[j];
							String tempStr2 = inputArr[j]; 
							
							int ai = j+1;
							while(ai < inputArr.length)
							{
								tempStr = tempStr.concat(inputArr[ai]);									//��ġ Ȯ�ο�
								tempStr2 = tempStr2.concat(" " +inputArr[ai]);							//���忡�� ������
								if(tempStr.equals(tempAdv))
								{
									found = true;
									if(times.contains("first"))
									{
										this.foundFirstAdv = advlist[i][0];
										this.foundFirstAdvDesc = advlist[i][1];
									}
									else if(times.contains("second"))
									{
										this.foundSecondAdv = advlist[i][0];
										this.foundSecondAdvDesc = advlist[i][1];
									}
									
									inputSentence = inputSentence.replace(tempStr2, "");			//�տ��� ��ó��. �迭 ��ȯ �� �λ� equals�� ã�� ��, �ٽ� ��ġ��. ã�� �λ��� �Է¹������� �����Ѵ�
									inputSentence = inputSentence.replace(" �� ", " ");				//�ӽ� ó��
									inputSentence = inputSentence.replace(" ���� ", " ");				//�ӽ� ó��
									inputSentence = inputSentence.replace(" ��ŭ ", " ");				//�ӽ� ó��
									break;
								}			
								ai++;
							}
							if(found)
								break;							
						}
					}					
				}
				if(found)
					break;
			}
		}
		
		//�����ð�ǥ�� ã��
		if(durationExp != null)
		{
			if(!durationExp[0].equals(""))															//�����ð�ǥ���� ���� ��쿡
			{
				if(this.foundFirstAdvDesc==null)												//�ٸ� �λ� ���� ���� ���ٸ�
					this.foundFirstAdvDesc = "DUR-"+duration+durationUnit;
				else																						//�ٸ� �λ� ���Ǿ��ٸ� "/"�� �����Ѵ�
					this.foundFirstAdvDesc += "/DUR-"+duration+durationUnit;
			}
		}
		
		//����ǥ�� ã��
		if(directionExp != null)
		{
			if(!directionExp[0].equals(""))															//����ǥ���� ���� ��쿡
			{
				if(this.foundFirstAdvDesc==null)												//�ٸ� �λ� ���� ���� ���ٸ�
					this.foundFirstAdvDesc = "DEG-"+direction+directionUnit;
				else																						//�ٸ� �λ� ���Ǿ��ٸ� "/"�� �����Ѵ�
					this.foundFirstAdvDesc += "/DEG-"+direction+directionUnit;
			}
		}
		
		return inputSentence;
	}
	
	
	/**
	 * ���� �ð� ǥ���� ã�´�
	 * �����ð�("" �Ǵ� ����), ����(��,��,�ð�), �����ð�ǥ���� ������ �Է¹��� ����� ������
	 * @param inputSentence
	 * @param inputArr
	 * @return
	 */
	private String[] findDurationTime(String inputSentence, String[] inputArr)
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
			if(inputArr[i].matches(regex)&&ckDurDirOrder(inputArr, i))														// [1] ���ڰ� �߰ߵǰ�,
			{
				time = inputArr[i];				
				durationStart = inputSentence.indexOf(inputArr[i]);
				for(int j=i; j<inputArr.length; j++)
				{					
					if(findUnitExp(inputArr, i, inputArr.length, new String[]{"��","��","�ð�"}))								// [2] �ڿ� ��, ��, �ð��� ���� ��쿡
					{						
						if(findUnitExp(inputArr, i, j, new String[]{"����"}))														//'5�� ����'�� '����'�� ã��
						{
							unitExp = getUnitExp(inputArr, i, inputArr.length, new String[]{"��","��","�ð�"});
							int durationEndStart = inputSentence.indexOf(time);							
							int unitExpArrIdx = findUnitExpIdx(inputArr, i, inputArr.length, new String[]{time});
							durationEnd = durationEndStart + this.findExpressionEndEnd(inputSentence, inputArr, unitExpArrIdx);	
							found = true;
							break;
						}
						else if(findUnitExp(inputArr, i, j, new String[]{"��"}))														//'5�� ��'�� '��'�� ã��
						{
							unitExp = getUnitExp(inputArr, i, inputArr.length, new String[]{"��","��","�ð�"});
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
		
		if(found)																			//�ð�ǥ���� �־��ٸ� �Է¹����� �ش� �κи� �����Ѵ�
		{
			inputSentence = deleteDirectionMsg(inputArr, inputSentence, time, durationStart, durationEnd);
			inputSentence = inputSentence.replaceFirst("����", "");
			inputSentence = inputSentence.replaceFirst("��", "");
		}
		
		if(found)																			//�����ð�ǥ���� �־��ٸ�
		{
			resultArr[0] = time;			
			resultArr[1] = unitExp;
			resultArr[2] = inputSentence;
		}
		else																				//�����ð�ǥ���� �߰ߵ��� �ʾҴٸ�
		{
			resultArr[0] = "";
			resultArr[1] = "";
			resultArr[2] = inputSentence;
		}
		
		return resultArr;
	}
	
	
	/**
	 * ���� ǥ���� ã�´�
	 * "����("" �Ǵ� ����), ����(��), ����ǥ���� ������ �Է¹�"�� ����� ������
	 * @param inputSentence
	 * @param inputArr
	 * @return
	 */
	private String[] findDirection(String inputSentence, String[] inputArr)
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
			if(inputArr[i].matches(regex)&&ckDirDurOrder(inputArr, i))																		// [1] ���ڰ� �߰ߵǰ�,
			{
				direction = inputArr[i];		
				directionStart = inputSentence.indexOf(inputArr[i]);
				for(int j=i; j<inputArr.length; j++)
				{
					if(findUnitExp(inputArr, i, inputArr.length, new String[]{"����","��"}))			// [2] �ڿ� '����, ��'�� ���� ���
					{						
						if(findUnitExp(inputArr, i, j, new String[]{"��"}))									//'4�� ����'�� '��'�� ã��
						{
							unitExp = "��";
							int unitExpArrIdx = findUnitExpIdx(inputArr, i, inputArr.length, new String[]{"����","��"});
							int directionEndStart = inputSentence.indexOf(inputArr[unitExpArrIdx]);
							directionEnd = directionEndStart + this.findExpressionEndEnd(inputSentence, inputArr, unitExpArrIdx);
							found = true;
							break;
						}
						else if(findUnitExp(inputArr, i, j, new String[]{"��"}))							//'30�� ����'�� '��'�� ã��
						{
							unitExp = "��";
							int unitExpArrIdx = findUnitExpIdx(inputArr, i, inputArr.length, new String[]{"����","��"});
							int directionEndStart = inputSentence.indexOf(inputArr[unitExpArrIdx]);
							directionEnd = directionEndStart + this.findExpressionEndEnd(inputSentence, inputArr, unitExpArrIdx);
							found = true;
							break;
						}						
					}
					else
					{
						if(inputArr[j].equals("��"))														// '����, ��' ǥ�� ���� ���� '30�� ����'�� '��'�� ã��
						{
							unitExp = "��";
							int unitExpArrIdx = findUnitExpIdx(inputArr, i, inputArr.length, new String[]{"��"});
							int directionEndStart = inputSentence.indexOf(inputArr[unitExpArrIdx]);
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
		
		if(found)																					//����ǥ���� �־��ٸ� �Է¹����� �ش� �κи� �����Ѵ�
			inputSentence = deleteDirectionMsg(inputArr, inputSentence, direction, directionStart, directionEnd);
		
		if(found)																					//����ǥ���� �־��ٸ�
		{
			if(unitExp.equals("��"))															//"4�� ����"�� ���� ���Ǿ��ٸ�.
				resultArr[0] = String.valueOf(Integer.parseInt(direction) * 30);	//4�� --> 120�� �� ġȯ
			else if(unitExp.equals("��"))													//"30�� ����"�� ���� ���Ǿ��ٸ�
				resultArr[0] = direction;
			else																					//�� �ܿ��� ��� ���� ǥ���� "30�� ����"�� '��'�� ���Ǿ��ٰ� �����Ѵ�.
				resultArr[0] = direction;				
			resultArr[1] = "��";
			resultArr[2] = inputSentence;
		}
		else																						//����ǥ���� �߰ߵ��� �ʾҴٸ�
		{
			resultArr[0] = "";
			resultArr[1] = "";
			resultArr[2] = inputSentence;
		}
		
		return resultArr;
	}
	

	
	/**
	 * ������ �ð�ǥ������ �տ� �������� Ȯ���Ѵ�
	 * @param inputArr
	 * @param arrNum
	 * @return
	 */
	private boolean ckDurDirOrder(String[] inputArr, int arrNum)
	{
		boolean ok = true;
		
		for(int i=arrNum; i<inputArr.length; i++)
		{
			if(inputArr[i].equals("��")||inputArr[i].equals("��")||inputArr[i].equals("�ð�")||inputArr[i].equals("����"))
			{
				ok = true;
				break;
			}
			else if(inputArr[i].equals("��")||inputArr[i].equals("����")||inputArr[i].equals("��"))
			{
				ok = false;
				break;
			}
		}
		
		return ok;
	}	
	
	
	/**
	 * �ð��� ����ǥ������ �տ� �������� Ȯ���Ѵ�
	 * @param inputArr
	 * @param arrNum
	 * @return
	 */
	private boolean ckDirDurOrder(String[] inputArr, int arrNum)
	{
		boolean ok = true;
		
		for(int i=arrNum; i<inputArr.length; i++)
		{
			if(inputArr[i].equals("��")||inputArr[i].equals("��")||inputArr[i].equals("�ð�")||inputArr[i].equals("����"))
			{
				ok = false;
				break;
			}
			else if(inputArr[i].equals("��")||inputArr[i].equals("����")||inputArr[i].equals("��"))
			{
				ok = true;
				break;
			}
		}
		
		return ok;
	}
	
	
	
	/**
	 * �ð�/����ǥ���� ���κ��� ã�´�. (��: '�������� 4�� �������� �ø���'���� '��������'�� �� ��ġ)
	 * @param inputSentence
	 * @param inputArr
	 * @param unitExpArrIdx
	 * @return
	 */
	private int findExpressionEndEnd(String inputSentence, String[] inputArr, int unitExpArrIdx)
	{
		int findDirectionEndLength=0;
		String[] inputSentenceArr = inputSentence.split(" ");
		
		for(int i=0; i<inputSentenceArr.length; i++)
		{
			if(inputSentenceArr[i].startsWith(inputArr[unitExpArrIdx]))
				findDirectionEndLength = inputSentenceArr[i].length();
		}
		
		return findDirectionEndLength;
	}
	
	
	/**
	 * �迭 i���� j ���̿� Ư�� ǥ���� �ִ����� Ȯ���Ѵ�
	 * @param inputArr
	 * @param i
	 * @param j
	 * @param unitExp
	 * @return
	 */
	private boolean findUnitExp(String[] inputArr, int i, int j, String[] unitExp)
	{
		boolean found = false;
		for(int x=0; x<unitExp.length; x++)
		{
			for(int k=i; k<j; k++)
			{
				if(inputArr[k].equals(unitExp[x]))
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
	 * �迭 i���� j ���̿� �ִ� Ư�� ǥ���� �����Ѵ�
	 * @param inputArr
	 * @param i
	 * @param j
	 * @param unitExp
	 * @return
	 */
	private String getUnitExp(String[] inputArr, int i, int j, String[] unitExp)
	{
		String expression = "";
		boolean found = false;
		for(int x=0; x<unitExp.length; x++)
		{
			for(int k=i; k<j; k++)
			{
				if(inputArr[k].equals(unitExp[x]))
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
	 * �迭 i���� j ������ Ư�� ǥ���� �ִ� �迭��ġ�� �ľ��Ѵ�
	 * @param inputArr
	 * @param i
	 * @param j
	 * @param unitExp
	 * @return
	 */
	private int findUnitExpIdx(String[] inputArr, int i, int j, String[] unitExp)
	{
		int arrIdx = 0;
		boolean found = false;
		
		for(int x=0; x<unitExp.length; x++)
		{
			for(int k=i; k<j; k++)
			{
				if(inputArr[k].equals(unitExp[x]))
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
	 * �Է¹����� ����ǥ�� �κ��� �����Ѵ�
	 * @param inputArr
	 * @param inputSentence
	 * @param direction
	 * @param directionStart
	 * @param directionEnd
	 * @return
	 */
	private String deleteDirectionMsg(String[] inputArr, String inputSentence, String direction, int directionStart, int directionEnd)
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
	 * ����� �ִ����� üũ�ϰ�, ������� �� ���ľ Ŭ���� ������ ��´�
	 * @param inputArr	�Է¹� �迭
	 * @param objectlist	������ �ĺ� ����Ʈ
	 * @return
	 */
	private boolean ckHaveObject(String[] inputArr, String[] objectlist)
	{
		boolean found = false;		
		
		for(int i=0; i<inputArr.length; i++)											//��� ǥ���� �ϳ��� �����Ѵ�.
		{			
			for(int j=0; j<objectlist.length; j++)
			{
				if(inputArr[i].equals(objectlist[j]))										//����� �ش��ϴ� ���� �����ϸ�
				{
					found = true;
					this.foundFirstObject = makeObjectArr(inputArr, i);		//�߰ߵ� ù��° ����� Ŭ���� ������ ��´�
					break;
				}
			}
			if(found)
				break;
		}

		return found;
	}

	
	/**
	 * ������� �� ���ľ ã�� �迭�� ����� 
	 * @param inputArr	�Է¹� �迭
	 * @param objNum	����� �ִ� �迭 ��ȣ
	 * @return
	 */
	private String[] makeObjectArr(String[] inputArr, int objNum)
	{
		String[] objArr = null;
		
		if(objNum==0)													//�߰ߵ� ����� �迭�� ó���� �־ �� �̻� ���ľ ���� �� ���� ���
		{
			objArr = new String[1];
			objArr[0] = inputArr[objNum];
		}
		else if(objNum==1)												//�߰ߵ� ����� �迭�� �� ��°�� �־ �� �տ� �ϳ��� ���ľ ���� �� �ִ� ���
		{
			if(inputArr[objNum].equals("��")||inputArr[objNum].equals("��")||inputArr[objNum].equals("��")||inputArr[objNum].equals("��"))
			{
				if(inputArr[objNum-1].equals("��")||inputArr[objNum-1].equals("����")||inputArr[objNum-1].equals("��")||inputArr[objNum-1].equals("��")||inputArr[objNum-1].equals("�Ʒ�"))
				{
					objArr = new String[2];
					objArr[0] = inputArr[objNum-1];					//��, ����, ...
					objArr[1] = inputArr[objNum];					//��, ��, ��
				}
				else															//���ľ ������ ������ ����� ����Ѵ� 
				{
					objArr = new String[1];
					objArr[0] = inputArr[objNum];					//��, ��, ��
				}
			}
		}
		else																	//�߰ߵ� ����� �迭�� �� ��° ���Ͽ� �־ �� �տ� �� ���� ���ľ ���� �� �ִ� ���
		{
			if(inputArr[objNum].equals("��")||inputArr[objNum].equals("��")||inputArr[objNum].equals("��")||inputArr[objNum].equals("��"))
			{
				if(inputArr[objNum-1].equals("��"))					//�翬�� ��, ���� ���� ���ľ ���� ���̴� 
				{
					objArr = new String[3];
					objArr[0] = inputArr[objNum-2];					//��, ����, ...
					objArr[1] = inputArr[objNum-1];					//��
					objArr[2] = inputArr[objNum];					//��, ��, ��
				}				
				else if(inputArr[objNum-1].equals("��")||inputArr[objNum-1].equals("����")||inputArr[objNum-1].equals("��")||inputArr[objNum-1].equals("��")||inputArr[objNum-1].equals("�Ʒ�"))
				{
					objArr = new String[2];
					objArr[0] = inputArr[objNum-1];					//��, ����, ...
					objArr[1] = inputArr[objNum];					//��, ��, ��
				}
				else															//���ľ ������ ������ ����� ����Ѵ� 
				{
					objArr = new String[1];
					objArr[0] = inputArr[objNum];					//��, ��, ��
				}
			}
		}
		
		return objArr;
	}
	
	/**
	 * �Էµ� �迭�� �տ� ���ο� ���ָ� �߰��մϴ�
	 * @param inputArr	���� �迭
	 * @param lastObject �߰��� ����
	 * @return
	 */
	private String[] includeObject(String[] inputArr, String[] newWord)
	{
		if(newWord != null)																				//newWord�� null�� �ƴ� ��쿡��
		{
			int objArrLen = newWord.length;
			
			if(objArrLen==1)
			{
				if(newWord[0] != null)																	//newWord 0�� null�� �ƴ� ��쿡��
				{
					inputArr = (String[])resizeArray(inputArr, inputArr.length+1, 1);			//�� �տ� �� �ڸ��� �� ä ���� �迭�� ���̸� �ϳ� �ø���,
					inputArr[0] = newWord[0];															//���� ��ȭ�� ����� ó���� �Է��Ѵ�
				}		
			}
			else if(objArrLen==2)
			{
				if(newWord[1] != null)																	//newWord 1�� null�� �ƴ� ��쿡��
				{
					inputArr = (String[])resizeArray(inputArr, inputArr.length+2, 2);			//�� �տ� �� �ڸ��� �� ä ���� �迭�� ���̸� �ϳ� �ø���,
					inputArr[0] = newWord[0];															//���� ��ȭ�� ����� ó���� �Է��Ѵ�
					inputArr[1] = newWord[1];															//���� ��ȭ�� ������ ���ľ �Է��Ѵ�
				}		
			}
			else if(objArrLen>2)
			{
				if(newWord[2] != null)																	//newWord 2�� null�� �ƴ� ��쿡��
				{
					inputArr = (String[])resizeArray(inputArr, inputArr.length+3, 3);			//�� �տ� �� �ڸ��� �� ä ���� �迭�� ���̸� �ϳ� �ø���,
					inputArr[0] = newWord[0];															//���� ��ȭ�� ����� ó���� �Է��Ѵ�
					inputArr[1] = newWord[1];															//���� ��ȭ�� ������ ���ľ �Է��Ѵ�
					inputArr[2] = newWord[2];															//���� ��ȭ�� ������ ���ľ �Է��Ѵ�
				}		
			}
		}
		
		return inputArr;
	}	
	
	/**
	 * ���Ҹ� �ϳ� �� ���� �� �ֵ��� inputArr�� ũ�⸦ �ø��ϴ�
	 * @param oldArray ����ϴ� inputArr
	 * @param newSize ũ�Ⱑ �þ inputArr
	 * @param newArrStart ���ο� inputArr�� ä�� ��ȣ 
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
	 * ���� �Էµ��� ���� ���ְ� �´����� Ȯ���Ѵ� 
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
				break;					//���� �迭�� null�̶�� �� �˻��� �ʿ䰡 ���� 
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
	 * 2���� �迭�� n��° ���([n])�� ���� ũ�⿡ ���� ������������ �����մϴ�.
	 * @param expreeArrCnt
	 * @return
	 */
	public int[][] sort2DArr(GetNeededMorph gm, int[][] expreeArrCnt)
	{
		gm.sort2DArrayBy2ndNum(expreeArrCnt, 1, true);
		
		return expreeArrCnt;
	}
	
	
	/**
	 * �ĺ� ǥ������ ����Ѵ�.
	 * @param expressArrPart
	 * @param expression
	 * @param foundAdvDesc
	 */
	public void printCandidateExpression(int[][] expressArrPart, String[][] expression, String foundAdvDesc)
	{
		int foundNum = expressArrPart[0][1];
		System.out.println("����� ��Ұ� "+foundNum+"�� �߰ߵ� ǥ����");
		
		for(int i=0; i<expressArrPart.length; i++)
		{
			if(expressArrPart[i][1]<foundNum)
			{
				System.out.println("\n����� ��Ұ� "+expressArrPart[i][1]+"�� �߰ߵ� ǥ����");
				foundNum = expressArrPart[i][1];
			}
			System.out.println(expressArrPart[i][0]+"�� : " + expression[expressArrPart[i][0]][0] + "\t�λ��: " + foundAdvDesc);
		}
	}
	
	/**
	 * ���ĵ� �迭�� ������� ���� �ִ� �迭�� ��ȣ�� ã���ϴ�.
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
				if(!originalArr[i][j].equals(sortedArr[foundNum][j]))						//���� ���� ���� �߰ߵǸ� �ߴ�
					break;
				else
					innerSameCnt++;															//���ٸ� ī��Ʈ 
				
				if(innerSameCnt==sortedArr[foundNum].length)						//sortedArr�� ���� ����
				{
					if(((originalArr[i].length)-1)==sortedArr[foundNum].length)
					{
						wholeSame = true;
						foundArrNum = originalArr[i][originalArr[i].length-1];		//�� �迭 ������ ��ϵ� ���� �����´�
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
	 * �Է¹��� ��������� ������ �پ��ִ� ���, �̸� ����.
	 * @param input �Է¹�
	 * @param cklist �� ������� ���
	 * @return
	 */
	private String splitVx(String input, String[] cklist)
	{
		for(int i=0; i<cklist.length; i++)							//��� üũ����Ʈ�� ���Ͽ� �����Ѵ�.
		{
			if(input.contains(cklist[i]))								//üũ����Ʈ ������ �ִٸ�
			{
				int chIdx = input.indexOf(cklist[i]);
				if(chIdx==0)												//�� ������ ���� �����̸� �ش����
					;
				else
				{
					char ch = input.charAt(chIdx-1);
					if(ch==' ')											//�� ������ �� ���� ���� ���� �����̸� �ش����
						;							
					else													//������ �ƴ϶��, ������ �������
					{
						if(this.ckIsVx(input, chIdx))					//Ȯ�� ��� ��������� �ƴϰų�, Ȯ�� ��� ��������̸鼭 �˻縦 ����ߴٸ�
							input = input.substring(0, chIdx) + " " + input.substring(chIdx, input.length());
					}
				}
			}
		}
		
		return input;
	}
	
	
	/**
	 * ��������� �´��� Ȯ���ϴ� �Լ�
	 * @param input
	 * @param chIdx
	 * @return
	 */
	private boolean ckIsVx(String input, int chIdx)
	{
		boolean isVx = true;
		String[] conEomi = {"��", "��", "��", "��", "��", "��", "��", "��"};
		String[] ckVx = {"��", "��", "��", "��", "��", "��", "��", "��"};					//�� ���������� ��������� �ƴ� �� �����Ƿ� Ȯ���� �ؾ� �Ѵ�.
		
		for(int i=0; i<ckVx.length; i++)
		{
			if(String.valueOf(input.charAt(chIdx)).equals(ckVx[i]))						//���� ������ Ȯ�� ��� ��������� �´ٸ�
			{
				isVx = false;																		//�ϴ� ����� false�� �������´�.
				for(int j=0; j<conEomi.length; j++)
				{
					if(String.valueOf(input.charAt(chIdx-1)).equals(conEomi[j]))		//�� �� ������ �����̶��
					{
						isVx = true;
						break;
					}
				}
				if(isVx)																			//ã������ �ٷ� �����Ѵ�.
					break;
			}
		}		
		
		return isVx;
	}
	
	
	
	/**
	 * �� ���ո��� ����Ʈ�� �����Ͽ� �Է¹� �Ǵ� ������ ������ ���ϴ�.
	 * @param input
	 * @return
	 */
	private String splitComplexNoun(String input, FileIO fio)
	{
		String[][] cklist2 = fio.makeFileAllContents2DArray("./dic/com/", "cklist2.txt", "UTF8", 2);
		
		for(int i=0; i<cklist2.length; i++)
		{
			if(input.contains(cklist2[i][0]))
				input = input.replaceAll(cklist2[i][0], cklist2[i][1]);
		}
		
		return input;
	}
	
	
	/**
	 * �� ���� ����Ʈ�� �����Ͽ� �Է¹� �Ǵ� ������ ������ ���ϴ�.
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
	


	/**
	 * ������ ���¸� �м��� �°� ��ȯ�մϴ�.
	 * ��, choice ������ ���ؼ��� ������ ���¸� ��ȯ���� �ʽ��ϴ�.
	 * @param originalDicPath
	 * @param dictionary
	 * @param cklist
	 * @param ckPicknumlist
	 * @param encoding
	 * @return
	 */
	public String cleanDictionary(String originalDicPath, String dictionary, String[] cklist, String[] ckPicknumlist, String encoding)
	{
		FileIO fio = new FileIO();
		String[][] expression = fio.makeFileAllContents2DArrayBySign(originalDicPath, dictionary, encoding, 2, ",");
		
		if(dictionary.equals("choice.CSV"))													//choice.CSV �� �Ϻ� ������ �Ʒ��� ���ո��� �и�, ������� �и��� ���� �ʴ´�.
			;
		else if(dictionary.equals("picknum.CSV"))											//picknum.CSV �� ����� �� ���� �и�
		{
			for(int i=0; i<expression.length; i++)
				expression[i][0] = this.splitNumberNoun(expression[i][0], fio);
		}
		else																							//�׿� �ٸ� ������ ������ ���ؼ��� ���ո��� �и�, ������� �и��� �����Ѵ�.
		{
			for(int i=0; i<expression.length; i++)
				expression[i][0] = this.splitComplexNoun(expression[i][0], fio);		//����� �� ���ո��� �и�. ������ --> ���� ��
			
			for(int i=0; i<expression.length; i++)
				expression[i][0] = this.splitVx(expression[i][0], cklist);					//������� �и�
		}
		
		StringBuilder sb = new StringBuilder();		
		for(int i=0; i<expression.length; i++)
			sb.append(expression[i][0]+","+expression[i][1]+"\r\n");
		
		return sb.toString();
	}
	
	
	/**
	 * ������� �λ� ���� ���� �迭�� �ѹ��� ������ �ɴϴ�.
	 * @param expression
	 * @param highArrNum
	 * @param objlist
	 * @param advlist
	 * @return
	 */
	public int findMostHighObjAdv(String[][] expression, int[][] highArrNum, String[] objlist, String[][] advlist)
	{
		int mostHighArrNum = 0;											//���� ���� ���� ������� �λ� ������ �ִ� �迭
		int currentHighScore = 0;											//���� ���� ���� ������� �λ���� ��
		
		for(int i=0; i<highArrNum.length; i++)
		{
			String dic_a_col = expression[highArrNum[i][0]][0];		//���¼� �м��� ������ ����
			int objscore = 0;
			int advscore = 0;
			
			String dic_a_col_copyA = dic_a_col;						//������ Ž���� ���� ī�Ǻ�
			for(int j=0; j<objlist.length; j++)
			{
				if(dic_a_col_copyA.contains(objlist[j]))
				{
					objscore++;
					dic_a_col_copyA = dic_a_col_copyA.replace(objlist[j], "");
				}
			}
			
			String dic_a_col_copyB = dic_a_col;							//�λ�� Ž���� ���� ī�Ǻ�
			for(int j=0; j<advlist.length; j++)
			{
				if(dic_a_col_copyB.contains(advlist[j][0]))
				{
					advscore++;
					dic_a_col_copyB = dic_a_col_copyB.replace(advlist[j][0], "");
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
	
	
	/**
	 * �Է¹��� ���Ͽ� ���ո���� ������� ���⸦ �����մϴ�.
	 * ��, choice ���� � ���Ͽ��� �̷��� ���� �۾��� �������� �ʰ�, ������ �״�� �ٽ� �����մϴ�.
	 * @param dicType ������ Ÿ��
	 * @param input �Է¹�
	 * @param fio FileIO ��ü
	 * @param ckVxlist ������� ����Ʈ
	 * @return
	 */
	public String splitComplexNounNVxOfInput(String dicType, String input, FileIO fio, String[] ckVxlist)
	{
		if(dicType.equals("choice"))								
		{
			;																// choice�� ���ؼ��� �ƹ� �۾��� ���� �ʴ´�
		}
		else if(dicType.equals("picknum"))
		{
			input = this.splitNumberNoun(input, fio);
		}
		else
		{
			input = this.splitComplexNoun(input, fio);		//�Է¹��� ����� �� ���ո��簡 �ִ� ���, ����.
			input = this.splitVx(input, ckVxlist);					//�Է¹��� ��������� �ִ� ���, ����.
			input = this.changeNumExpToIntExp(input);
		}		
		return input;
	}
	
	
	/*
	 * �ѱ��� ���ڽð��� �ƶ��� ���ڽð� ǥ������ �ٲ۴�
	 */
	private String changeNumExpToIntExp(String input)
	{
		input = input.replace("�ѽ�", "1��");
		input = input.replace("�ν�", "2��");
		input = input.replace("����", "3��");
		input = input.replace("�׽�", "4��");
		input = input.replace("�ټ���", "5��");
		input = input.replace("������", "6��");
		input = input.replace("�ϰ���", "7��");
		input = input.replace("������", "8��");
		input = input.replace("��ȩ��", "9��");
		input = input.replace("����", "10��");
		input = input.replace("���ѽ�", "11��");
		input = input.replace("���ν�", "12��");
		
		input = input.replace("�� ��", "1��");
		input = input.replace("�� ��", "2��");
		input = input.replace("�� ��", "3��");
		input = input.replace("�� ��", "4��");
		input = input.replace("�ټ� ��", "5��");
		input = input.replace("���� ��", "6��");
		input = input.replace("�ϰ� ��", "7��");
		input = input.replace("���� ��", "8��");
		input = input.replace("��ȩ ��", "9��");
		input = input.replace("�� ��", "10��");
		input = input.replace("���� ��", "11��");
		input = input.replace("���� ��", "12��");
		return input; 
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
	 * �ؼ��� �ݴ�� �ٲ��� ǥ���� �ִ��� Ȯ���Ͽ� ������ �ٲ��ش�
	 * @param input
	 * @param arrNum
	 * @param expression
	 * @param expressArr_Original
	 * @return
	 */
	public String[][] findAdverseExpression(String input, int arrNum, String[][] expression, String[][] expressArr_Original)
	{
		boolean found = false;
		if(input.contains("��"))														//�Է¹��� '��' ǥ���� �־��ٸ�
		{
			for(int i=0; i<expressArr_original[arrNum].length; i++)
			{
				if(expressArr_original[arrNum][i].equals("��"))					//�׷��� �߰ߵ� ������ ���뿡�� '��' ǥ���� �־��ٸ�
				{
					found = true;														//�߰�ǥ���ϰ�, �� Ư���� ó���� ���� �ʴ´�
					break;
				}
			}
			
			if(!found)																	//�׷��� �Է¹����� '��'�� �־�����, �������� �����ٸ�
			{
				if(expression[arrNum][1].trim().equals("����"))
					expression[arrNum][1] = "����";
				else if(expression[arrNum][1].trim().equals("����"))
					expression[arrNum][1] = "����";
			}
		}
		
		return expression;
	}

}
