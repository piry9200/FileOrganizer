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
 * このプログラムは，任意のディレクトリに存在するファイルを他のファイルに
 * 日付別に分類するクラス
 */
public class FileOrganizer {
    private File originDir;
    private File destDir;
    private SubDirFormatters subDirFormatter = SubDirFormatters.YEAR_MONTH_DAY;
    private FileNameFormatters fileNameFormatter = FileNameFormatters.HOUR_MINUTE_SECOND;

    // test
    /**
     *　コンストラクタ
     * @param originDir:読み込み先のディレクトリを表すFile型
     * @param destDir:保存先のディレクトリを表すFile型
     */
    public FileOrganizer(File originDir, File destDir) {
        setOriginDir(originDir);
        setDestDir(destDir);
    }

    /**
     * this.originDirのSetter
     * @param originDir
     */
    public void setOriginDir(File originDir) {
        this.originDir = originDir;
    }

    /**
     * this.destDirのSetter
     * @param destDir
     */
    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }

    /**
     * this.subDirFormatterのSetter
     * @param subDirFormatter
     */
    public void setSubDirFormatters(SubDirFormatters subDirFormatter) {
        this.subDirFormatter = subDirFormatter;
    }

    /**
     * this.fileNameFormatterのSetter
     * @param fileNameFormatter
     */
    public void setFileNameFormatter(FileNameFormatters fileNameFormatter) {
        this.fileNameFormatter = fileNameFormatter;
    }

    /**
     * 保存先に生成するサブディレクトリの構造を定義する定数を管理
     */
    public enum SubDirFormatters {
        YEAR_MONTH_DAY("yyyy/MM/dd"),
        YEAR_MONTH_WEEK("yyyy/MM/第W週");
        private final SimpleDateFormat subDirFormatter;

        private SubDirFormatters(String format) {
            this.subDirFormatter = new SimpleDateFormat(format);
        }

        public SimpleDateFormat getSubDirFormatter() {
            return subDirFormatter;
        }
    }

    /**
     * コピーするファイルのファイル名に加える情報を設定する定数を管理
     */
    public enum FileNameFormatters {
        HOUR_MINUTE_SECOND("hh時mm分ss秒::"),
        NONE("");
        private final SimpleDateFormat fileNameFormatter;

        private FileNameFormatters(String format) {
            this.fileNameFormatter = new SimpleDateFormat(format);
        }

        public SimpleDateFormat getFileNameFormatter() {
            return fileNameFormatter;
        }
    }

    /**
     * ファイルの分類を実行するメソッド．
     * 呼び出すことで，フィールド変数に登録した「読み込み先ディレクトリ」，「保存先ディレクトリ」，
     * 「保存するサブディレクトリの構造」，「ファイル名の設定」に従って
     * 処理を実行する．
     */
    public void organizeFiles() {
        try (final Stream<Path> filePathsStream = Files.walk(originDir.toPath().toAbsolutePath())) {
            final List<Path> filePathsList = filePathsStream.filter(Files::isRegularFile).toList();

            //読み込み先にあるファイルの総数を取得する
            final int numFiles = filePathsList.size();
            //保存先ディレクトリへ，ファイルをいくつコピーしたかをカウントする
            int counter = 0;

            //読み込み先のディレクトリ内に存在するファイルらに対して，処理をするfor文．
            for (final Path filePath : filePathsList) {
                final FileArgs fileArgs = new FileArgs(filePath);
                //ファイルを配置するためのdestDir下のサブディレクトリのパスを取得あるいは作成
                final Path destSubDirPath = getOrCreateSubDirPath(destDir, fileArgs);
                copyFileToSubDir(destSubDirPath, fileArgs);

                System.out.printf("処理率 %.1f %% | %d/%d(処理済/総数) | 処理中→ %s \n",
                        ((double) counter / (numFiles)) * 100, counter, numFiles, fileArgs.file.getName());

                counter++;
            }
            System.out.printf("%d個のデータをコピーして移動させました", counter);

        } catch (IOException e) {
            System.out.println(e);
            System.out.println("エラー：ファイルを読み込めませんでした．プログラムを終了します．");
            System.exit(1);
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

    // dir 下に date で指定された日時情報を用いてサブディレクトを取得または作成する．そのサブディレクトの構造はenumである「SubDirFormatters」で指定する．
    private Path getOrCreateSubDirPath(File dir, FileArgs fileArgs){
        Date date = fileArgs.fileCl.getTime();
        SimpleDateFormat formatter = this.subDirFormatter.getSubDirFormatter();
        final String destSubDirStr = dir.getPath() + "/" + formatter.format(date);
        final Path destSubDirPath = Paths.get(destSubDirStr);

        //departure_dirに，対象ファイルが作成された年月日の名前のディレクトリがなかったらそのディレクトリを作る
        if (!Files.exists(destSubDirPath)) {
            try { //一致するディレクトリが無かったら実行
                Files.createDirectories(destSubDirPath);
            } catch (IOException e) {
                System.out.printf("エラー：%sを保存するサブディレクトリを作成する際に以下のエラーが生じました\n", fileArgs.fileName);
                System.out.println(e);
            }
        }

        return destSubDirPath;
    }

    private void copyFileToSubDir(Path destSubDirPath, FileArgs fileArgs) throws IOException{
        //名前を一意に決定するために，作成された時間をファイルネームに追加する．
        SimpleDateFormat formatter = this.fileNameFormatter.getFileNameFormatter();
        final String fileDateStr = formatter.format(fileArgs.fileCl.getTime());
        final String fileName = fileDateStr + fileArgs.file.getName();
        //対象ファイルのコピー先の絶対パスをPath型で定義(ファイル名の例 neko)
        final Path fileDestPath = Paths.get(destSubDirPath.toString() + "/" + fileName);

        try {
            Files.copy(fileArgs.file.getAbsoluteFile().toPath(), fileDestPath);
            //コピーした時間が最終更新時間になってしまうので、元ファイルの最終更新日時をセットする
            fileDestPath.toFile().setLastModified(fileArgs.file.lastModified());
        } catch (IOException e) {
            System.out.printf("エラー：%s を %s へコピーする際に以下のエラーが生じました", fileArgs.fileName, destSubDirPath.toString());
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
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

        fileOrg.organizeFiles();

    }
}
