package kr.ac.hansung.d2e.D2EClassifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class GetNeededMorph 
{
	private String[] outputArr;       
    
    /**
     * ���¼� �м� ��� ��, cklist�� �ش��ϴ� �κи� 1���� �迭�� ������ �ɴϴ�.
     * GetOutputPartArr_Stem()�� ���� �Ϲ����� �����Դϴ�.
     * �ش��ϴ� ����Ʈ�� �ܺο��� ������ üũ�մϴ�. cklist�� ���� ������ �����ϴ�.
     * String[] cklist = {"NNG", "NNP", "NNB", "����", "����"};
     * @param output
     * @param cklist
     * @return
     */
    public String[] GetOutputPartArr_cklist(String output, String[] cklist)
    {
    	String[] outputPartArr = new String[GetMorphLength(output)]; 	
    	String[] outputArr = this.GetOutputArr();
    	
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
			
			try{
				String[] part2 = part1[1].split(" \\+ ");
				for(int i2=0; i2<part2.length; i2++)							//part2�� "����/MAG" �� ���� ���·� �Ǿ� �ִ�.
				{
					for(int j=0; j<cklist.length; j++)							//�˻��ؾ� �� ����Ʈ�� ��ȸ�Ѵ�.
					{
						if(part2[i2].contains(cklist[j]))
						{
							if(part2[i2].contains("/"))							//�������� ���
							{
								String[] part3 = part2[i2].split("/");
								outputPartArr[m] = part3[0];
								
								if(i2<part2.length-1)								//���������� m ���� �ø��� �ʴ´�.
									m++;					
								
								break;
							}
							else															//����ó��
							{							
								outputPartArr[m] = eojul;
								
								if(i2<part2.length-1)								//���������� m ���� �ø��� �ʴ´�.
									m++;		
							}									
						}			
					}
				}		
			}catch(Exception e){ System.out.println("GetOutputPartArr_StemPlus2()���� ������ �߻��߽��ϴ�.\r\n����� �迭�� �������� ���� �⺻ ���¼� �м� ����� ���ؼ��� �����մϴ�."); e.printStackTrace();}
		}				
    	
    	outputPartArr = removeNullValue(outputPartArr);    	
    	return outputPartArr;
    }    
    
    private String[] GetOutputArr()
    {
    	return outputArr;
    }
    
    
	/**
	 * �迭���� null ���� �����մϴ�.
	 * @param targetArr
	 * @return
	 */
	public String[] removeNullValue(String[] targetArr)
	{
		targetArr = Arrays.stream(targetArr)							// null �迭 ����
                .filter(s -> (s != null && s.length() > 0))
                .toArray(String[]::new);
		
		return targetArr;		
	}	
		
	
	/**
	 * 2���� �迭�� n��° ��ҿ��� 0�� �ִ� ���� �����մϴ�.
	 * @param array
	 * @return
	 */
	public int[][] remove0Value2D(int[][] array, int n)
	{
		int[][] newArray = null;
		
		int targetIndex = -1;
		for(int sourceIndex = 0;  sourceIndex < array.length;  sourceIndex++)
		{
		    if(array[sourceIndex][n] != 0)
		    {
		    	targetIndex += 1;
		        array[targetIndex][0] = array[sourceIndex][0];
		        array[targetIndex][1] = array[sourceIndex][1];
		    }
		}
		
		if(targetIndex==-1)								//����� ��Ұ� ���� ���� ���
			newArray = new int[0][2];
		else													//����� ��Ұ� �ϳ� �̻� �ִ� ���
		{
			newArray = new int[targetIndex][2];
			System.arraycopy(array, 0, newArray, 0, targetIndex);
		}		
		
		return newArray;
	}	 
		
	
	/**
	 * �������� ���ĵ� 2���� �迭�� n��° ��Ұ� ���� ���� �͸��� ���մϴ�. 
	 * @param array
	 * @param n
	 * @param threshold �������� ����(>=)
	 * @return
	 */
	public int[][] getHighNumber2D(int[][] array, int n, int threshold)
	{
		ArrayList<int[]> list = new ArrayList<int[]>();
		int high = array[0][n];
		
		if(high >= threshold) 
		{
			for(int i=0; i<array.length; i++)
			{
				if(array[i][n] == high)
					list.add(new int[]{array[i][0], array[i][1]});
				else
					break;				
			}	
		}
		int[][] newArray = list.toArray(new int[list.size()][2]);  		
		return newArray;
	}
	
	
    /**
     * ���¼��� ���� ����մϴ�.
     * @param output
     * @return
     */
    private int GetMorphLength(String output)
	{
    	outputArr = output.split("\r\n");			 
    	int outputArrLength = 0;
    	
    	for(int i=0; i<outputArr.length; i++)
    	{
    		if(outputArr[i].equals(""))
    			;
    		else
    			outputArrLength++;    			
    	}    	
    	
		int morphNum = 0;		
		for(int i=0; i<outputArrLength; i++)
		{
			if(outputArr[i].contains(" + "))
			{
				String[] part = outputArr[i].split(" \\+ ");
				morphNum += part.length;
			}
			else
				morphNum++;
		}
		
		return morphNum;
	}
    
    
	/**
	 * 2���� �迭�� ������ ���� �������� �����մϴ�.
	 * ��) Object arr2D2[][] = {{"���� �ϳ�"}, {"���� ��", 65}, {"���� ��", 84, "..."}};
	 * sort2DArrayByLength(arr2D2, true);
	 * @param arr 2���� �迭
	 * @param descending ������������ ����
	 */
	public void sort2DArrayByLength(Object[][] arr, boolean descending)
    {
		Arrays.sort(arr, new Comparator<Object[]>()
        {
            public int compare(Object[] arr1, Object[] arr2) 
            {            	
            	int result = 0;					//0�� ����
            	int desc = -1; 					//���Ⱑ -1�μ� ��� ����Ǹ� �������� ������ �ϰ� �ȴ�.
            	
            	if(descending)					//�������� �����̸� �� ���� 1�� �ٲ۴�.
            		desc = 1;            		
            	if( ((Comparable<Integer>)arr1.length).compareTo(arr2.length) < 0 )
        			result = desc;	        
            	else if( ((Comparable<Integer>)arr1.length).compareTo(arr2.length) == 0 )	//���� ó���� �ش��Ѵ�. 
            		result = desc;            	
                else
                	result = -desc;
            	
            	return result;                
            }
        });
    }	
	
	
	
	/**
	 * 2���� �迭�� n��° ����� ũ�⿡ ���� �����մϴ�.
	 * @param arr
	 * @param n �� ���. 0��°~n��°
	 * @param descending True�̸� �������� ����
	 */
	public void sort2DArrayBy2ndNum(int[][] arr, int n, boolean descending)
    {
		Arrays.sort(arr, new Comparator<int[]>() 
        {
            public int compare(int[] arr1, int[] arr2) 
            {            	
            	int result = 0;					//0�� ����
            	int desc = -1; 					//���Ⱑ -1�μ� ��� ����Ǹ� �������� ������ �ϰ� �ȴ�.
            	
            	if(descending)					//�������� �����̸� �� ���� 1�� �ٲ۴�.
            		desc = 1;            		
            	
            	if( ((Comparable<Integer>)arr1[n]).compareTo(arr2[n]) < 0 )
        			result = desc;	        
            	else if( ((Comparable<Integer>)arr1[n]).compareTo(arr2[n]) == 0 )		//���� ó���� �ش��Ѵ�.
            		result = desc;
                else
                	result = -desc;
            	
            	return result;                
            }
        });
    }	

    
    
	

}
