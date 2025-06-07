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
 */

public class FileOrganizer {
    public static final String DEFAULT_FILE_NAME_FORMAT = "%2d時%2分%2秒::%s";
    private String fileNameFormat = DEFAULT_FILE_NAME_FORMAT;
    private File originDir;
    private File destDir;

    public FileOrganizer(File originDir, File destDir) {
        setOriginDir(originDir);
        setDestDir(destDir);
    }

    public void setFileNameFormat(String file_name_format) {
        this.fileNameFormat = file_name_format;
    }

    public void setOriginDir(File originDir) {
        this.originDir = originDir;
    }

    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }

    // dir 下に date で指定されて年・月・時間別にディレクトリをのパスを返す．無ければ作成する．
    private Path getOrCreateSubDirPath(File dir, Date date){
        final SimpleDateFormat subDirDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        final String destSubDirStr = dir.getPath() + subDirDateFormat.format(date);
        final Path destSubDirPath = Paths.get(destSubDirStr);

        //departure_dirに，対象ファイルが作成された年月日の名前のディレクトリがなかったらそのディレクトリを作る
        if (!Files.exists(destSubDirPath)) {
            try { //一致するディレクトリが無かったら実行
                Files.createDirectories(destSubDirPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return destSubDirPath;
    }

    public void organizeFiles() throws IOException {
        //「整理したいファイルが入っているディレクトリ」のファイルたちを配列にする
        try (final Stream<Path> filePathsStream = Files.walk(originDir.toPath().toAbsolutePath())) {
            final List<Path> filePathsList = filePathsStream.filter(Files::isRegularFile).toList();

            //読み込み先にあるファイルの総数を取得する
            final int numFiles = filePathsList.size();
            //保存先ディレクトリへ，ファイルをいくつコピーしたかをカウントする
            int counter = 0;

            //読み込み先のディレクトリ内に存在するファイルらに対して，処理をするfor文．
            for (final Path filePath : filePathsList) {
                final FileArgs fileArgs = new FileArgs(filePath);
                //各ファイルを配置するためのディレクトリのパスを取得
                Path destSubDirPath = getOrCreateSubDirPath(destDir, fileArgs.fileCl.getTime());

                //名前を一意に決定するために，作成された時間をファイルネームに追加する．
                SimpleDateFormat fileNameDateFormat = new SimpleDateFormat("hh時mm分ss秒");
                final String fileDateStr = fileNameDateFormat.format(fileArgs.fileCl.getTime());
                final String fileName =  fileDateStr + "::" + fileArgs.file.getName();
                //対象ファイルのコピー先の絶対パスをPath型で定義(ファイル名の例 neko)
                final Path fileDestPath = Paths.get(destSubDirPath.toString() + "/" + fileName);

                System.out.printf("処理率 %.1f %% | %d/%d(処理済/総数) | 処理中→ %s \n",
                        ((double) counter / (numFiles)) * 100, counter, numFiles, fileArgs.file.getName());

                try {
                    Files.copy(fileArgs.file.getAbsoluteFile().toPath(), fileDestPath);
                    //コピーした時間が最終更新時間になってしまうので、元ファイルの最終更新日時をセットする
                    fileDestPath.toFile().setLastModified(fileArgs.file.lastModified());
                    counter++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.printf("%d個のデータをコピーして移動させました", counter);

        } catch (IOException e) {
            throw e;
        }
    }

    private class FileArgs {
        final File file;
        final Calendar fileCl;
        String fileName;
        public FileArgs(Path filePath) {
            this.file = new File(filePath.toString());
            this.fileName = this.file.getName();
            Date fileDate = new Date(this.file.lastModified());
            this.fileCl = Calendar.getInstance(TimeZone.getTimeZone("JST"));
            this.fileCl.setTime(fileDate);
        }
    }

    public static void main(String[] args) throws IOException {
        final Scanner scanner = new Scanner(System.in);
        final File originDir;
        final File destDir;

        while (true) {
            System.out.print("読み込みたいディレクトリの絶対パスを入力してください(「control+c」で中断)： ");
            final String originDirPath = scanner.nextLine();
            if (FileOrgUtil.isExistDir(originDirPath)) {
                originDir = new File(originDirPath);
                break;
            } else {
                System.out.println("エラー：そのパスはディレクトリではありません\n");
            }
        }

        while (true) {
            System.out.print("保存先のディレクトリの絶対パスを入力してください(「control+c」で中断)： ");
            String destDirPath = scanner.nextLine();
            if (FileOrgUtil.isExistDir(destDirPath)) {
                destDir = new File(destDirPath);
                break;
            } else {
                System.out.println("エラー：そのパスはディレクトリではありません\n");
            }
        }

        final var fileOrg = new FileOrganizer(originDir, destDir);

        try {
            fileOrg.organizeFiles();
        } catch (IOException e) {
            System.out.println("ファイルのコピー中にエラーが生じました");
            System.out.println(e);
        }

    }
}
