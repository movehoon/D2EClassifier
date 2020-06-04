package kr.ac.hansung.d2e.D2EClassifier;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import linguaFile.FileIO;
import rhino.RHINO;

public class MainClass
{
	private Gson gson;

	private RHINO rn;
	private GeneralTools gt;
	private PreProcessor pp;
	private MakeQuestion mq;
	private FileIO fio;
	private FindExpression fx;

	private int colNum;
	private String ckVxlistStr;
	private String ckPicknumlistStr;
	private String[] ckVxlist;
	private String[] ckPicknumlist;
	private String objectlistStr;
	private String[] objectlist;
	private String[][] advlist;
	private String[] cklist;

	private String[][] expression;
	private String[][] expressArr;
	private String[][] expressArr_Original;
	private String[] lastObject;

	private Interface_Movements im;
	private Interface_Choice ic;
	private Interface_Yesno iy;
	private Interface_Picknum ip;

	private String dicType;
	String dicPath = "./dic/";


	/**
	 * 생성자 함수
	 * 모든 종류의 화행분석기에 대한 공통 설정을 수행한다
	 * 화행분석기를 처음 시작할 때 한 번 수행한다
	 */
	public MainClass()
	{
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");											// 이 설정을 해주어야 배열 정렬이 이루어진다
		gson = new Gson();

		rn = new RHINO();
		rn.ExternInit("Java");

		gt = new GeneralTools();
		pp = new PreProcessor();
		mq = new MakeQuestion();
		fio = new FileIO();

		this.ckVxlistStr = fio.openResult(dicPath.concat("com/"), "cklist_vx.txt", "euc-kr");							//띄어쓸 보조용언 목록 가져오기
		this.ckPicknumlistStr = fio.openResult(dicPath.concat("com/"), "cklist_picknum.txt", "euc-kr");		//띄어쓸 picknum 목록 가져오기
		this.ckVxlist = ckVxlistStr.split("\r\n");
		this.ckPicknumlist = ckPicknumlistStr.split("\r\n");

		this.objectlistStr = fio.openResult(dicPath.concat("com/"), "cklist_obj.txt", "euc-kr");						//검토할 목적어의 목록 가져오기
		this.objectlist = objectlistStr.split("\r\n");
	}


	/**
	 * 화행분석기 종류에 따른 설정을 수행한다
	 * 화행분석기의 종류를 바꿀 때는 이 함수를 새로 수행해야 한다
	 * 현재는 화행분석기 종류를 yesno-choice-yesno 와 같이는 사용하지 않는 것을 상정했다. 이렇게 해도 되나 다만 이 설정함수를 새로 수행해야 한다
	 * @param dicType							// yesno(긍부정), movements(동작코칭), picknum(숫자맞추기), choice(초이스블럭)
	 */
	public void setDictypeProperty(String dicType)
	{
		this.dicType = dicType;

		if(dicType.equals("yesno"))
			this.cklist = new String[] {"NNG", "NNP", "NNB", "NP", "NR", "VV", "VA", "VCN", "MM", "MAG", "MAJ", "IC", "XR", "SL", "SH", "NF", "NV", "SN", "겠", "ㄹ까", "을까", "ㄹ게", "VX"};
		else if(dicType.equals("choice"))
			this.cklist = new String[] {"NNG", "NNP", "NR", "SN", "MM"};			//명사류와 숫자만 준비해둔다. "첫, 두, 세, 네"는 MM이다.
		else
			this.cklist = new String[] {"NNG", "NNP", "NNB", "NP", "NR", "VV", "VA", "VCN", "MM", "MAG", "MAJ", "IC", "XR", "SL", "SH", "NF", "NV", "SN", "겠", "ㄹ까", "을까", "ㄹ게"};

		this.colNum = gt.getCSVFirstLineColNum(dicPath, this.dicType.concat(".CSV"), "euc-kr");

		//FindExpression 클래스는 DicType에 따라 다르게 구성된다
		fx = new FindExpression(this.cklist);

		//advlist [0]입력문에서의 원형태, [1]동작명세, [2]형태소분석된 형태
		this.advlist = fx.getAdvList(rn, fio.openResult(dicPath.concat("com/"), "movements_clean.CSV", "euc-kr"));						//부사어의 목록 만들기. com 폴더가 아닌 원 파일을 이용해야 한다(부사어는 확인 및 제거를 위해 cleaning 이전이어야 함)

		//사전의 형태를 분석에 맞게 변환하기 (단, choice.CSV는 변환작업 없이 _clean.CSV를 만든다)
		//함수 내부에서 사전별로 다른 방식의 cleaning이 적용되니, 새로운 사전을 적용할 경우 반드시 확인해야 한다
		String dic_clean = pp.cleanDictionary(rn, dicPath, dicType.concat(".CSV"), ckVxlist, ckPicknumlist, "euc-kr");
		fio.saveResult(dic_clean, dicPath.concat("com/"), dicType.concat("_clean.CSV"), "euc-kr", false, "");			//변환된 사전을 저장하기

		//쉼표 구분이 아닌 다른 방식으로 구분이 되었다면 이곳의 ','를 다른 것으로 바꾼다
		this.expression = fio.makeFileAllContents2DArrayBySign(dicPath.concat("com/"), dicType.concat("_clean.CSV"), "euc-kr", colNum, ",");			// 경로, 불러올 파일 이름, 인코딩, 컬럼의 수, 컬럼 구분 기호

		fx.sortExpressionDictionaryAndCopy(rn, fio, expression);				//어근 사전 생성. 결과는 fx.getExpressionArr()와 fx.getExpressArr_Original() 함수로 가지고 온다.
		this.expressArr = fx.getExpressArr();
		this.expressArr_Original = fx.getExpressArr_Original();
		this.lastObject = null;															//마지막 대화에서 사용한 목적어

		if(dicType.equals("movements"))
			im = new Interface_Movements();
		else if(dicType.equals("choice"))
			ic = new Interface_Choice();
		else if(dicType.equals("yesno"))
			iy = new Interface_Yesno();
		else if(dicType.equals("picknum"))
			ip = new Interface_Picknum();
	}


	/**
	 * 분석 후 결과물을 출력한다
	 * @param input
	 * @param jsonType
	 * @return
	 */
	public String getOuput(String input, boolean jsonType)
	{
		//예외 처리
		if(this.dicType.equals("choice")&&jsonType==false)
		{
			System.out.println("choice는 반드시 json 형태로 입력해야 합니다");
			System.out.println("System Quit... Bye");
			System.exit(0);
		}

		if(!jsonType)
			input = "{\"method\":\""+this.dicType+"\", \"input\":\""+input+"\"}";			// template을 사용하지 않을 때는 이 부분을 주석처리

		JsonElement element = gson.fromJson (input, JsonElement.class);
		JsonObject jsonObject = element.getAsJsonObject();
		//dicType = jsonObject.get("method").getAsString();												//이것으로 보아도 되지만, 이미 앞에서 설정하게 되어 있다
		String output = "NOT_FOUND";

		if(this.dicType.equals("movements"))
			output = im.classsInterface(jsonObject, fio, pp, fx, rn, mq, dicType, this.cklist, this.ckVxlist, this.expression, this.expressArr, this.expressArr_Original, this.objectlist, this.lastObject, this.advlist);
		else if(this.dicType.equals("choice"))
			output = ic.classsInterface(jsonObject, fio, pp, fx, rn, mq, dicType, this.cklist, this.ckVxlist, this.expression, this.expressArr, this.expressArr_Original, this.objectlist, this.lastObject, this.advlist);
		else if(this.dicType.equals("yesno"))
			output = iy.classsInterface(jsonObject, fio, pp, fx, rn, mq, dicType, this.cklist, this.ckVxlist, this.expression, this.expressArr, this.expressArr_Original, this.objectlist, this.lastObject, this.advlist);
		else if(this.dicType.equals("picknum"))
			output = ip.classsInterface(jsonObject, fio, pp, fx, rn, mq, dicType, this.cklist, this.ckVxlist, this.expression, this.expressArr, this.expressArr_Original, this.objectlist, this.lastObject, this.advlist);

		return output;
	}


}
