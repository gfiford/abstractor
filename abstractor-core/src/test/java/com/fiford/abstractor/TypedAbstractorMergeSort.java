package com.fiford.abstractor;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import com.fiford.Abstractor.Mailbox;
import com.fiford.Abstractor.TypedActor;


public class TypedAbstractorMergeSort {
    public static void main(String[] args) {
        Random random = new Random(0L);
        int[] input = IntStream.range(0, 1 << 16).map(i -> random.nextInt()).toArray();
        System.err.println("Abstractor merge sort started...");
        long start = System.currentTimeMillis();
        TypedActor<Sorter> sorter = new TypedActor<TypedAbstractorMergeSort.Sorter>(new Sorter());
        sorter.ask(s -> s.run(input)).recieve().get();
        long end = System.currentTimeMillis();
        System.err.println("finished in " + (end - start));
    }

    private static class Sorter {

        public int[] run(int[] array) {
            if (array.length == 1) return array;

            final int[] left = Arrays.copyOfRange(array, 0, array.length / 2);
            final int[] right = Arrays.copyOfRange(array, array.length / 2, array.length);

            if (array.length == 2) return merge(left, right);
            
            TypedActor<Sorter> a = new TypedActor<>(new Sorter());
            TypedActor<Sorter> b = new TypedActor<>(new Sorter());

            Mailbox<int[]> lBox =  a.ask(s -> s.run(left));
            Mailbox<int[]> rBox =  b.ask(s -> s.run(right));
                
            return merge(lBox.recieve().get() , rBox.recieve().get());
            
        }

        public static int[] merge(int[] a, int[] b) {
            int[] answer = new int[a.length + b.length];
            int i = a.length - 1, j = b.length - 1, k = answer.length;
            while (k > 0)
                answer[--k] = (j < 0 || (i >= 0 && a[i] >= b[j])) ? a[i--] : b[j--];
            return answer;
        }

    }

}
