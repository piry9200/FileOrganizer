package aitech.maslab.piry.fileorganizer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * このプログラムは，任意のディレクトリに存在するファイル（）
 *
 */

public class FileOrganizer {
    private final static Scanner scanner = new Scanner(System.in);
    private static File originDir;
    private static File destDir;

    public static void main(String[] args) {
        FileOrganizer FileOrg = new FileOrganizer();
        System.out.println("読み込みたいディレクトリの絶対パスを入力してください(「control+c」で中断)： ");
        FileOrg.originDir = setTargetDir();
        System.out.println("保存先のディレクトリの絶対パスを入力してください(「control+c」で中断)： ");
        FileOrg.destDir = setTargetDir();

        //「整理したいファイルが入っているディレクトリ」のファイルたちを配列にする
        List<File> files = null;
        try {
            files = createFileList(FileOrg.originDir);
        }catch (RuntimeException e){
            System.out.println("エラー：ディレクトをファイルとして読み込もうとしました");
        }

        //保存先ディレクトリへ，ファイルをいくつコピーしたかをカウントする
        int counter = 0;

        //読み込み先のディレクトリ内に存在するファイルらに対して，処理をするfor文．
        for(File file: files){
            if(file.isDirectory()) continue;
            //対象ファイルの作成日時を取得
            Date raw_date = new Date(file.lastModified());
            SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss/SS");
            //↓ [0]:year,[1]:month,[2]:day,[3]:hour,[4]:minute,[5]:secondsに日付を分割
            String[] dates = date.format(raw_date).split("/");

            //departure_dirに対象ファイルが作成された、年月日の名前のディレクトリがなかったらそのディレクトリを作る
            if(!Files.exists(Paths.get(destDir.getPath() + "/" + dates[0] + "/" + dates[1] + "/" + dates[2]))){
                try { //一致するディレクトリが無かったら実行
                    Files.createDirectories(Paths.get(destDir.getPath() + "/" + dates[0] + "/" + dates[1] + "/" + dates[2]));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            String file_name =  dates[0] + ":" + dates[1] + ":" + dates[2] + ":" + dates[3] + "時" + dates[4] + "分" + dates[5]  + "秒";
            //対象ファイルの絶対パスをPath型で定義
            //Path file_orign_path = Paths.get(originDir.getPath() + "/" + file.getName());
            //対象ファイルの移動先の絶対パスをPath型で定義
            Path file_destPath = Paths.get(destDir.getPath() + "/" + dates[0] + "/" + dates[1] + "/" + dates[2] + "/" + file_name + file.getName());
            try{
                Files.copy(file.getAbsoluteFile().toPath(), file_destPath);
                file_destPath.toFile().setLastModified(file.lastModified()); //コピーした時間が最終更新時間になってしまうので、元ファイルの最終更新日時をセットする
                counter++;
            }catch (IOException e){
                e.printStackTrace();
            }
            System.out.printf("%d個目のファイルを完了\n", counter);
        }
        System.out.printf("%d個のデータをコピーして移動させました", counter);

    }

    private static File setTargetDir(){
        while (true){
            String path = scanner.nextLine();

            try {
                File targetDir = new File(path);
                if(targetDir.isDirectory()) {
                    return targetDir;
                }else{
                    System.out.println("エラー：ディレクトリを指定してください\n");
                }
            }catch (NullPointerException e) {
                System.out.println("エラー：存在しないパスです\n");
            }
        }
    }

    private static List<File> createFileList(File targetDir) throws RuntimeException{
        // ディレクトリ以外のFile型が来たらエラーを投げる
        if(! targetDir.isDirectory()) throw new RuntimeException();

        //サブディレクトリ内のファイルも含むリスト．最終的にこのリストがreturnされる．
        List<File> files = new ArrayList<>();

        // 指定されたディレクトリ直下のファイルを含むリスト
        List<File> tempFiles = Arrays.asList(targetDir.listFiles());

        for(File file: tempFiles){
            // サブディレクトリ内のファイルを取得できるように処理
            if(file.isDirectory()){
                files.addAll(createFileList(file));
            }else{
                files.add(file);
            }
        }
        return files;
    }



}
