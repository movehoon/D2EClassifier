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

    static String dicType = "movements";

    static Scanner sc = new Scanner(System.in);
    static MainClass mc = new MainClass();			//Main Class의 객체는 한 번만 생성한다. Main Class 객체를 생성할 때마다 형태소분석기가 새로 만들어진다


    public static void main(String[] args) {
        System.setProperty("tomcat.util.http.parser.HttpParser.requestTargetAllow", "{}");
		System.setProperty("java.awt.headless", "false");
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

		SpringApplication.run(D2EClassifierApplication.class, args);

        mc.setDictypeProperty(dicType);				//화행분석기 종류에 따른 설정을 수행한다. 화행분석기의 종류를 바꿀 때는 이 함수를 새로 수행해야 한다

        while (true) {
			try {
                // Check json format at "http://json.parser.online.fr"
                // {"method":"yesno", "input":"맞는 것 같애"}
                // {"method":"movements", "input":"오른팔을 위로 올려봐"}
                // {"method":"picknum", "input":"사십칠이야"}
                // {"method":"choice", "input":{"choice": ["오타와","토론토","몬트리올","밴쿠버","서울"], "answer": "정답은 오타와인 것 같아"}}
				System.out.print("\r\nInput: ");
				String sc_input = sc.nextLine().trim().replaceAll("\\s+", " ");

                String output = GetResult(sc_input, true);
                System.out.println("input:" + sc_input + ", output: " + output);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static String GetResult(String input, Boolean jsonType) {
        String result = "";
        try {
            Gson gson = new Gson();
            JsonElement element = gson.fromJson (input, JsonElement.class);
            JsonObject jsonObject = element.getAsJsonObject();
            String newDicType = jsonObject.get("method").getAsString();
            System.out.println("type:" + newDicType);

            if (!dicType.equals(newDicType)) {
                System.out.println("Change type to " + newDicType);
                mc.setDictypeProperty(newDicType);
                dicType = newDicType;
            }
            result = mc.getOuput(input, jsonType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
