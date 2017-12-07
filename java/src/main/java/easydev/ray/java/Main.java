package easydev.ray.java;

import java.util.Arrays;

public class Main {
    public static void main(String... args){
        sort(2, 1, 3);
    }

    static void sort(int... ags){
        Arrays.sort(ags);
        System.out.println(ags);
    }
}

