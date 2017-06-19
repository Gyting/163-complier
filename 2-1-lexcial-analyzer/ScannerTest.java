package my.scanner;

import org.junit.Test;

public class ScannerTest {


    @Test
    public void test() {
        Scanner.parese("src/my/scanner/input");
    }
    
   //测试nextChar()是否能够正确的读入文件,正确处理文件结束的情况
    @Test
    public void testReadFile(){
       /* 
         while(true){
            char c = scanner.nextChar(scanner.rf);
            System.out.print(c);
            if(c=='$'){
                break;
            }
        }
       */
    }
    
    
    //测试rollBack()是否正确回滚一个字符
    @Test
    public void testRollBack(){
        /*
        System.out.print(scanner.nextChar(scanner.rf));
        scanner.rollBack(scanner.rf);
        System.out.print(scanner.nextChar(scanner.rf));
        */
    }
}
