package kr.ac.hansung.d2e.D2EClassifier;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import linguaFile.FileIO;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import rhino.RHINO;

import java.util.Scanner;

@SpringBootApplication
public class D2EClassifierApplication {

	static String dicPath = "./dic/";
	static boolean findSimilarExpressionWithOrder = false;				//유사한 표현을 찾을 때 사전의 순서와 같은 표현만 찾을지 여부

	static Scanner sc = new Scanner(System.in);

	static Interface_Movements im = new Interface_Movements();
	static Interface_Choice ic = new Interface_Choice();
	static Interface_Yesno iy = new Interface_Yesno();
	static Interface_Picknum ip = new Interface_Picknum();

	public static void main(String[] args) {
		System.setProperty("java.awt.headless", "false");
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

		SpringApplication.run(D2EClassifierApplication.class, args);

		while (true) {
			try {
				System.out.print("\r\nInput: ");
				String sc_input = sc.nextLine().trim();

				String dicType = getType(sc_input);
				String input = getInput(sc_input);
				System.out.println("\r\n[" + dicType + "]" + input);

				String result = process(sc_input);
				System.out.println("[Result]" + result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	static public String getType(String json) {
		String type = "";
		try {
			Gson gson = new Gson();
			JsonElement element = gson.fromJson (json, JsonElement.class);
			JsonObject jsonObject = element.getAsJsonObject();
			type = jsonObject.get("method").getAsString();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return type;
	}

		static public String getInput(String json) {
		String input = "";
		try {
			Gson gson = new Gson();
			JsonElement element = gson.fromJson (json, JsonElement.class);
			JsonObject jsonObject = element.getAsJsonObject();
			input = jsonObject.get("input").toString();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return input;
	}

		static public String process(String input) {
		String output = "";

		String dicType = getType(input);

		GeneralTools gt = new GeneralTools();
		String[] cklist;
		if(dicType.equals("yesno"))
			cklist = new String[] {"NNG", "NNP", "NNB", "NP", "NR", "VV", "VA", "VCN", "MM", "MAG", "MAJ", "IC", "XR", "SL", "SH", "NF", "NV", "SN", "겠", "ㄹ까", "을까", "ㄹ게", "VX"};
		else
			cklist = new String[] {"NNG", "NNP", "NNB", "NP", "NR", "VV", "VA", "VCN", "MM", "MAG", "MAJ", "IC", "XR", "SL", "SH", "NF", "NV", "SN", "겠", "ㄹ까", "을까", "ㄹ게"};
		FindExpression fx = new FindExpression(cklist);
		MakeQuestion mq = new MakeQuestion();
		FileIO fio = new FileIO();

		RHINO rn = new RHINO();
		rn.ExternInit("Java");

		String ckVxlistStr = fio.openResult(dicPath.concat("com/"), "cklist.txt", "euc-kr");									//띄어쓸 보조용언 목록 가져오기
		String ckPicknumlistStr  = fio.openResult(dicPath.concat("com/"), "cklist_picknum.txt", "euc-kr");			//띄어쓸 picknum 목록 가져오기
		String[] ckVxlist = ckVxlistStr.split("\r\n");
		String[] ckPicknumlist = ckPicknumlistStr.split("\r\n");
		String[][] advlist = fx.getAdvList(fio.openResult(dicPath, "movements.CSV", "euc-kr"));							//부사어의 목록 만들기. com 폴더가 아닌 원 파일을 이용해야 한다(부사어는 확인 및 제거를 위해 cleaning 이전이어야 함)

		String dic_clean = fx.cleanDictionary(dicPath, dicType.concat(".CSV"), ckVxlist, ckPicknumlist, "euc-kr");	//사전의 형태를 분석에 맞게 변환하기 (단, choice.CSV는 변환작업 없이 _clean.CSV를 만든다)
		fio.saveResult(dic_clean, dicPath.concat("com/"), dicType.concat("_clean.CSV"), "euc-kr", false, "");			//변환된 사전을 저장하기

		//쉼표 구분이 아닌 다른 방식으로 구분이 되었다면 이곳의 ','를 다른 것으로 바꾼다
		int colNum = gt.getCSVFirstLineColNum(dicPath, dicType.concat(".CSV"), "euc-kr");
		String[][] expression = fio.makeFileAllContents2DArrayBySign(dicPath.concat("com/"), dicType.concat("_clean.CSV"), "euc-kr", colNum, ",");			// 경로, 불러올 파일 이름, 인코딩, 컬럼의 수, 컬럼 구분 기호

		String objectlistStr = fio.openResult(dicPath.concat("com/"), "cklist3.txt", "euc-kr");								//검토할 목적어의 목록 가져오기
		String[] objectlist = objectlistStr.split("\r\n");
		fx.sortExpressionDictionaryAndCopy(rn, fio, expression);				//어근 사전 생성. 결과는 fx.getExpressionArr()와 fx.getExpressArr_Original() 함수로 가지고 온다.
		String[][] expressArr = fx.getExpressArr();
		String[][] expressArr_Original = fx.getExpressArr_Original();
		String[] lastObject = null;														//마지막 대화에서 사용한 목적어

		Gson gson = new Gson();
		JsonElement element = gson.fromJson (input, JsonElement.class);
		JsonObject jsonObject = element.getAsJsonObject();
		dicType = jsonObject.get("method").getAsString();

		if(dicType.equals("movements"))
			output = im.classsInterface(sc, jsonObject, fio, fx, rn, mq, dicType, input, ckVxlist, expression, expressArr, expressArr_Original, objectlist, lastObject, advlist);
		else if(dicType.equals("choice"))
			output = ic.classsInterface(sc, jsonObject, fio, fx, rn, mq, dicType, input, ckVxlist, expression, expressArr, expressArr_Original, objectlist, lastObject, advlist);
		else if(dicType.equals("yesno"))
			output = iy.classsInterface(sc, jsonObject, fio, fx, rn, mq, dicType, input, ckVxlist, expression, expressArr, expressArr_Original, objectlist, lastObject, advlist);
		else if(dicType.equals("picknum"))
			output = ip.classsInterface(sc, jsonObject, fio, fx, rn, mq, dicType, input, ckVxlist, expression, expressArr, expressArr_Original, objectlist, lastObject, advlist);

		return output;
	}

}
