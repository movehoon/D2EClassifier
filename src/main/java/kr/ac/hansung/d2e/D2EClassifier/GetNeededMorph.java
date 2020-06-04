package kr.ac.hansung.d2e.D2EClassifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class GetNeededMorph
{
	private String[] outputArr;



	/**
	 * 형태소 분석 결과 중, cklist에 해당하는 부분만 1차원 배열로 가지고 옵니다.
	 * GetOutputPartArr_Stem()의 보다 일반적인 형태입니다.
	 * 해당하는 리스트를 외부에서 가져와 체크합니다. cklist의 예는 다음과 같습니다.
	 * String[] cklist = {"NNG", "NNP", "NNB", "ㄹ까", "을까"};
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
				for(int i2=0; i2<part2.length; i2++)							//part2는 "별로/MAG" 와 같은 형태로 되어 있다.
				{
					for(int j=0; j<cklist.length; j++)							//검사해야 할 리스트를 순회한다.
					{
						if(part2[i2].contains(cklist[j]))
						{
							if(part2[i2].contains("/"))								//정상적인 경우
							{
								String[] part3 = part2[i2].split("/");
								outputPartArr[m] = part3[0];

								if(i2<part2.length-1)								//마지막에는 m 값을 올리지 않는다.
									m++;

								break;
							}
							else														//예외처리
							{
								outputPartArr[m] = eojul;

								if(i2<part2.length-1)								//마지막에는 m 값을 올리지 않는다.
									m++;
							}
						}
					}
				}
			}catch(Exception e){ System.out.println("GetOutputPartArr_cklist()에서 오류가 발생했습니다."); e.printStackTrace();}
		}

		outputPartArr = removeNullValue(outputPartArr);
		return outputPartArr;
	}


	/**
	 * 형태소 분석 결과 중, cklist에 해당하는 부분만 2차원 배열로 가지고 옵니다.
	 * GetOutputPartArr_Stem()의 보다 일반적인 형태입니다.
	 * GetOutputPartArr_cklist() 함수에 비하여 원문의 어절정보를 추가로 가져옵니다.
	 * 해당하는 리스트를 외부에서 가져와 체크합니다. cklist의 예는 다음과 같습니다.
	 * String[] cklist = {"NNG", "NNP", "NNB", "ㄹ까", "을까"};
	 * @param output
	 * @param cklist
	 * @return
	 */
	public String[][] GetOutputPartArr_cklist2D(String output, String[] cklist)
	{
		String[][] outputPartArr = new String[GetMorphLength(output)][2];
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
				for(int i2=0; i2<part2.length; i2++)							//part2는 "별로/MAG" 와 같은 형태로 되어 있다.
				{
					for(int j=0; j<cklist.length; j++)							//검사해야 할 리스트를 순회한다.
					{
						if(part2[i2].contains(cklist[j]))
						{
							if(part2[i2].contains("/"))								//정상적인 경우
							{
								String[] part3 = part2[i2].split("/");
								outputPartArr[m][0] = part3[0];				//어절의 형태소 분석 결과
								outputPartArr[m][1] = String.valueOf(i);		//어절 번호

								if(i2<part2.length-1)								//마지막에는 m 값을 올리지 않는다.
									m++;

								break;
							}
							else														//예외처리
							{
								outputPartArr[m][0] = eojul;

								if(i2<part2.length-1)								//마지막에는 m 값을 올리지 않는다.
									m++;
							}
						}
					}
				}
			}catch(Exception e){ System.out.println("GetOutputPartArr_cklist2D()에서 오류가 발생했습니다."); e.printStackTrace();}
		}

		outputPartArr = removeNullValue2D(outputPartArr);
		return outputPartArr;
	}


	public String[] GetOutputArr()
	{
		return outputArr;
	}


	/**
	 * 배열에서 null 값을 삭제합니다.
	 * @param targetArr
	 * @return
	 */
	public String[] removeNullValue(String[] targetArr)
	{
		targetArr = Arrays.stream(targetArr)							// null 배열 삭제
				.filter(s -> (s != null && s.length() > 0))
				.toArray(String[]::new);

		return targetArr;
	}


	/**
	 * 2차원 배열에서 inner에서 null 값을 삭제합니다.
	 * @param targetArr
	 * @return
	 */
	public static String[][] removeNullValue2D( String[][] targetArr) {
		ArrayList<ArrayList<String>> list2d = new ArrayList<ArrayList<String>>();

		for(String[] arr1d: targetArr){
			ArrayList<String> list1d = new ArrayList<String>();
			for(String s: arr1d){
				if(s != null && s.length() > 0) {
					list1d.add(s);
				}
			}
			if(list1d.size()>0){
				list2d.add(list1d);
			}
		}
		String[][] cleanArr = new String[list2d.size()][];
		int next = 0;
		for(ArrayList<String> list1d: list2d){
			cleanArr[next++] = list1d.toArray(new String[list1d.size()]);
		}
		return cleanArr;
	}


	/**
	 * 2차원 배열의 n번째 요소에서 0이 있는 것을 삭제합니다.
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

		if(targetIndex==-1)								//공통된 요소가 전혀 없는 경우
			newArray = new int[0][2];
		else													//공통된 요소가 하나 이상 있는 경우
		{
			newArray = new int[targetIndex][2];
			System.arraycopy(array, 0, newArray, 0, targetIndex);
		}

		return newArray;
	}


	/**
	 * 내림차순 정렬된 2차원 배열의 n번째 요소가 가장 높은 것만을 취합니다.
	 * @param array
	 * @param n 비교 대상 2차원 n번 배열
	 * @param threshold 공통요소의 기준(>=)
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
	 * 형태소의 수를 계산합니다.
	 * @param output
	 * @return
	 */
	public int GetMorphLength(String output)
	{
		this.outputArr = output.split("\r\n");
		int outputArrLength = 0;

		for(int i=0; i<this.outputArr.length; i++)
		{
			if(this.outputArr[i].equals(""))
				;
			else
				outputArrLength++;
		}

		int morphNum = 0;
		for(int i=0; i<outputArrLength; i++)
		{
			if(this.outputArr[i].contains(" + "))
			{
				String[] part = this.outputArr[i].split(" \\+ ");
				morphNum += part.length;
			}
			else
				morphNum++;
		}

		return morphNum;
	}


	/**
	 * 2차원 배열을 원소의 수를 기준으로 정렬합니다.
	 * 예) Object arr2D2[][] = {{"원소 하나"}, {"원소 둘", 65}, {"원소 셋", 84, "..."}};
	 * sort2DArrayByLength(arr2D2, true);
	 * @param arr 2차원 배열
	 * @param descending 내림차순정렬 여부
	 */
	public void sort2DArrayByLength(Object[][] arr, boolean descending)
	{
		Arrays.sort(arr, new Comparator<Object[]>()
		{
			public int compare(Object[] arr1, Object[] arr2)
			{
				int result = 0;					//0은 같음
				int desc = -1; 					//여기가 -1로서 계속 진행되면 오름차순 정렬을 하게 된다.

				if(descending)					//내림차순 정렬이면 이 값을 1로 바꾼다.
					desc = 1;
				if( ((Comparable<Integer>)arr1.length).compareTo(arr2.length) < 0 )
					result = desc;
				else if( ((Comparable<Integer>)arr1.length).compareTo(arr2.length) == 0 )	//예외 처리에 해당한다.
					result = desc;
				else
					result = -desc;

				return result;
			}
		});
	}



	/**
	 * 2차원 배열을 n번째 요소의 크기에 따라 정렬합니다.
	 * @param arr
	 * @param n 비교 대상. 0번째~n번째
	 * @param descending True이면 내림차순 정렬
	 */
	public void sort2DArrayBy2ndNum(int[][] arr, int n, boolean descending)
	{
		Arrays.sort(arr, new Comparator<int[]>()
		{
			public int compare(int[] arr1, int[] arr2)
			{
				int result = 0;					//0은 같음
				int desc = -1; 					//여기가 -1로서 계속 진행되면 오름차순 정렬을 하게 된다.

				if(descending)					//내림차순 정렬이면 이 값을 1로 바꾼다.
					desc = 1;

				if( ((Comparable<Integer>)arr1[n]).compareTo(arr2[n]) < 0 )
					result = desc;
				else if( ((Comparable<Integer>)arr1[n]).compareTo(arr2[n]) == 0 )		//예외 처리에 해당한다.
					result = desc;
				else
					result = -desc;

				return result;
			}
		});
	}


}
