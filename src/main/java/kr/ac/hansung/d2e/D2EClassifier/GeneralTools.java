package kr.ac.hansung.d2e.D2EClassifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class GeneralTools
{
	/**
	 * 쉼표로 구분된 파일의 첫 줄을 읽어 컬럼의 수를 파악합니다.
	 * 따라서 첫 줄에는 내용에 쉼표가 있어서는 안됩니다.
	 * @param path
	 * @param filename
	 * @param encoding
	 * @return
	 */
	public int getCSVFirstLineColNum(String path, String filename, String encoding)
	{
		int colNum = 0;

		File fi = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;

		try{
			//대상 파일 읽기
			fi= new File(path+filename);
			is = new FileInputStream(fi);
			isr = new InputStreamReader(is, encoding);
			br = new BufferedReader(isr);

			String line=br.readLine();
			String[] lineArr = line.split(",");

			colNum = lineArr.length;
		}
		catch(NullPointerException nullPointerException)
		{   System.out.println("ERROR: 지정되지 않은 영역 오류 발생");   }
		catch(FileNotFoundException filesNotFoundException)
		{   System.out.println("ERROR: 파일이 발견되지 않았습니다");  }
		catch(IOException ioex)
		{   System.out.println("ERROR: IO 오류가 발생했습니다 " + ioex.toString());	     }
		catch (Exception e)
		{    e.printStackTrace();	System.out.println("ERROR: 오류가 발생했습니다");		}
		finally{ try {	br.close();	} catch (IOException e) {	e.printStackTrace();		}}

		return colNum;
	}


	/**
	 * 배열을 delim을 뒤에 붙여가며 하나의 문자열로 만듭니다.
	 * 단, 마지막 배열 뒤에는 delim을 붙이지 않습니다.
	 * 배열 원소가 하나이면 delim 없이 바로 출력합니다
	 * @param arr
	 * @param delim
	 * @return
	 */
	public String concatWithDelim(String[] arr, String delim)
	{
		String result = "";

		if(arr.length > 1)
		{
			for(int i=0; i<arr.length; i++)
			{
				if(i < arr.length-1)
					result = result.concat(arr[i]+delim);
				else
					result = result.concat(arr[i]);
			}
		}
		else if (arr.length > 0)
		{
			result = arr[0];
		}

		return result;
	}



	/**
	 * 2차원 arrayList를 2차원 배열로 변환해 출력한다
	 * @return
	 */
	public String[][] arrayList2DToArray2D(ArrayList<String[]> arrayList) {
		String[][] array = null;

		if(arrayList==null)
		{
			array = new String[0][];
		}
		else
		{
			array = new String[arrayList.size()][];

			for(int i=0; i<arrayList.size(); i++) {
				String[] row = arrayList.get(i);
				array[i] = new String[row.length];

				for(int j=0; j<row.length; j++)
					array[i][j] = row[j];
			}
		}

		return array;
	}


}
