package aitech.maslab.piry.fileorganizer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 * このプログラムは，任意のディレクトリに存在するファイル（）
 *
 */

public class FileOrganizer {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("読み込みたいディレクトリの絶対パスを入力してください");
        String originPath = scanner.nextLine();
        System.out.println("保存先のディレクトリの絶対パスを入力してください");
        String departurePath = scanner.nextLine();

        //整理したいファイルが入っているディレクトリ
        File originDir = new File(originPath);
        //「整理したいファイルが入っているディレクトリ」のファイルたちを配列にする
        File[] children = originDir.listFiles();
        //保存先ディレクトリへ，ファイルをいくつコピーしたかをカウントする
        int counter = 0;

        //読み込み先のディレクトリ内に存在するファイルらに対して，処理をするfor文．
        for(File file: children){
            if(file.isDirectory()) continue;
            //対象ファイルの作成日時を取得
            Date pre_date = new Date(file.lastModified());
            SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss/SS");
            //↓ [0]:year,[1]:month,[2]:day,[3]:hour,[4]:minute,[5]:secondsに日付を分割
            String[] dates = date.format(pre_date).split("/");

            //departure_dirに対象ファイルが作成された、年月日の名前のディレクトリがなかったらそのディレクトリを作る
            if(!Files.exists(Paths.get(departurePath + "/" + dates[0] + "/" + dates[1] + "/" + dates[2]))){
                try { //一致するディレクトリが無かったら実行
                    Files.createDirectories(Paths.get(departurePath + "/" + dates[0] + "/" + dates[1] + "/" + dates[2]));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            String file_name =  dates[0] + ":" + dates[1] + ":" + dates[2] + ":" + dates[3] + "時" + dates[4] + "分" + dates[5]  + "秒";
            //対象ファイルの絶対パスをPath型で定義
            Path file_orign_path = Paths.get(originPath + "/" + file.getName());
            //対象ファイルの移動先の絶対パスをPath型で定義
            Path file_departurePath = Paths.get(departurePath + "/" + dates[0] + "/" + dates[1] + "/" + dates[2] + "/" + file_name + file.getName());
            try{
                Files.copy(file_orign_path, file_departurePath);
                file_departurePath.toFile().setLastModified(file.lastModified()); //コピーした時間が最終更新時間になってしまうので、元ファイルの最終更新日時をセットする
                counter++;
            }catch (IOException e){
                e.printStackTrace();
            }
            System.out.printf("%d個目のファイルを完了\n", counter);
        }
        System.out.printf("%d個のデータをコピーして移動させました", counter);

    }
}
