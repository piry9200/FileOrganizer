package aitech.maslab.piry.fileorganizer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

/**
 * このプログラムは，任意のディレクトリに存在するファイル（）
 *
 */

public class FileOrganizer {
    private File originDir;
    private File destDir;

    FileOrganizer(File originDir, File destDir){
        setOriginDir(originDir);
        setDestDir(destDir);
    }

    public void setOriginDir(File originDir){
        this.originDir = originDir;
    }

    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }

    public void organizeFiles() throws IOException{
        //「整理したいファイルが入っているディレクトリ」のファイルたちを配列にする
        try(Stream<Path> filePathsStream = Files.walk(originDir.toPath().toAbsolutePath())){
            List<Path> filePathsList = filePathsStream.filter(Files::isRegularFile).toList();

            //読み込み先にあるファイルの総数を取得する
            int numFiles = filePathsList.size();
            //保存先ディレクトリへ，ファイルをいくつコピーしたかをカウントする
            int counter = 0;

            //読み込み先のディレクトリ内に存在するファイルらに対して，処理をするfor文．
            for(Path filePath: filePathsList){
                File file = new File(filePath.toString());
                //対象ファイルの作成日時を取得
                Date raw_date = new Date(file.lastModified());
                SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss/SS");
                //↓ [0]:year,[1]:month,[2]:day,[3]:hour,[4]:minute,[5]:secondsに日付を分割
                String[] dates = date.format(raw_date).split("/");

                //departure_dirに，対象ファイルが作成された年月日の名前のディレクトリがなかったらそのディレクトリを作る
                if(!Files.exists(Paths.get(destDir.getPath() + "/" + dates[0] + "/" + dates[1] + "/" + dates[2]))){
                    try { //一致するディレクトリが無かったら実行
                        Files.createDirectories(Paths.get(destDir.getPath() + "/" + dates[0] + "/" + dates[1] + "/" + dates[2]));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //名前を一意に決定するために，日時をファイルネームに追加する．
                String file_name =  dates[0] + ":" + dates[1] + ":" + dates[2] + ":" + dates[3] + "時" + dates[4] + "分" + dates[5]  + "秒";
                //対象ファイルのコピー先の絶対パスをPath型で定義
                Path file_destPath = Paths.get(destDir.getPath() + "/" + dates[0] + "/" + dates[1] + "/" + dates[2] + "/" + file.getName() + "::" + file_name);

                System.out.printf("処理率 %.1f %% | %d/%d(処理済/総数) | 処理中→ %s \n", ((double)counter/(numFiles))*100, counter, numFiles, file.getName());

                try{
                    Files.copy(file.getAbsoluteFile().toPath(), file_destPath);
                    file_destPath.toFile().setLastModified(file.lastModified()); //コピーした時間が最終更新時間になってしまうので、元ファイルの最終更新日時をセットする
                    counter++;
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            System.out.printf("%d個のデータをコピーして移動させました", counter);

        }catch (IOException e){
            throw e;
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        File originDir;
        File destDir;

        while (true){
            System.out.print("読み込みたいディレクトリの絶対パスを入力してください(「control+c」で中断)： ");
            String originDirPath = scanner.nextLine();
            if(FileOrgUtil.isExistDir(originDirPath)){
                originDir = new File(originDirPath);
                break;
            }else{
                System.out.println("エラー：そのパスはディレクトリではありません\n");
            }
        }

        while (true){
            System.out.print("保存先のディレクトリの絶対パスを入力してください(「control+c」で中断)： ");
            String destDirPath = scanner.nextLine();
            if(FileOrgUtil.isExistDir(destDirPath)){
                destDir = new File(destDirPath);
                break;
            }else{
                System.out.println("エラー：そのパスはディレクトリではありません\n");
            }
        }

        var fileOrg = new FileOrganizer(originDir, destDir);

        try{
            fileOrg.organizeFiles();
        }catch (IOException e){
            System.out.println("ファイルのコピー中にエラーが生じました");
            System.out.println(e);
        }

    }
}
