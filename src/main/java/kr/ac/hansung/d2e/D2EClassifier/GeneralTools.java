package kr.ac.hansung.d2e.D2EClassifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GeneralTools 
{
	
	
	/**
	 * ��ǥ�� ���е� ������ ù ���� �о� �÷��� ���� �ľ��մϴ�.
	 * ���� ù �ٿ��� ���뿡 ��ǥ�� �־�� �ȵ˴ϴ�.
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
            //��� ���� �б�
            fi= new File(path+filename);
            is = new FileInputStream(fi);
            isr = new InputStreamReader(is, encoding);
            br = new BufferedReader(isr);           

            String line=br.readLine();
            String[] lineArr = line.split(",");
            
            colNum = lineArr.length;            
        }
        catch(NullPointerException nullPointerException)
        {   System.out.println("ERROR: �������� ���� ���� ���� �߻�");   }
        catch(FileNotFoundException filesNotFoundException)
        {   System.out.println("ERROR: ������ �߰ߵ��� �ʾҽ��ϴ�");  }
        catch(IOException ioex)
        {   System.out.println("ERROR: IO ������ �߻��߽��ϴ� " + ioex.toString());	     }
        catch (Exception e) 
        {    e.printStackTrace();	System.out.println("ERROR: ������ �߻��߽��ϴ�");		}
        finally{ try {	br.close();	} catch (IOException e) {	e.printStackTrace();		}}	  
        
        return colNum;
	}

}
