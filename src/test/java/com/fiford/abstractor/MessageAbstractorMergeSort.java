package com.fiford.abstractor;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import com.fiford.ActorRef;
import com.fiford.MessageActor;
import com.fiford.MessageReciever;

public class MessageAbstractorMergeSort {
    public static void main(String[] args) {
        Random random = new Random(0L);
        int[] input = IntStream.range(0, 1 << 20).map(i -> random.nextInt()).toArray();
        System.err.println("Abstractor merge sort started...");
        long start = System.currentTimeMillis();
        MessageActor<SortMessage> messageActor = new MessageActor<>(new Sorter(-1));
        messageActor.ask(new SortMessage(SortMessage.Type.REQUEST, input, 0)).recieve().get().value();
        long end = System.currentTimeMillis();
        System.err.println("finished in " + (end - start));
    }

    public static record SortMessage(Type type, int[] value, int side) {
        enum Type {
            REQUEST, REPLY
        }
    }

    private static class Sorter implements MessageReciever<SortMessage> {

        private int side;
        private int[][] res = new int[2][];
        private ActorRef<SortMessage> upstream;

        Sorter(int side) {
            this.side = side;
        }

        void request(SortMessage m, ActorRef<SortMessage> u, MessageActor<SortMessage> self) {
            this.upstream = u;
            if (m.value.length == 1) {
                sendReply(m.value, self);
                return;
            }

            int[] left = Arrays.copyOfRange(m.value, 0, m.value.length / 2);
            int[] right = Arrays.copyOfRange(m.value, m.value.length / 2, m.value.length);

            sendSide(self, left, 0);
            sendSide(self, right, 1);

        }

        private void sendSide(ActorRef<SortMessage> self, int[] arr, int sde) {
            if (arr.length == 1) {
                // no need to send we know the reply
                reply(arr, sde, self);
            } else {
                MessageActor<SortMessage> a = new MessageActor<>(new Sorter(sde));
                a.tell(new SortMessage(SortMessage.Type.REQUEST, arr, sde), self);
            }
        }

        void reply(int[] arr, int sde, ActorRef<SortMessage> self) {
            res[sde] = arr;
            if (res[0] != null && res[1] != null) {
                int[] resultarray = merge(res[0], res[1]);
                sendReply(resultarray, self);
            }
        }

        private void sendReply(int[] resultarray, ActorRef<SortMessage> self) {
            upstream.tell(new SortMessage(SortMessage.Type.REPLY, resultarray, side), self);
        }

        public static int[] merge(int[] a, int[] b) {
            int[] answer = new int[a.length + b.length];
            int i = a.length - 1, j = b.length - 1, k = answer.length;
            while (k > 0)
                answer[--k] = (j < 0 || (i >= 0 && a[i] >= b[j])) ? a[i--] : b[j--];
            return answer;
        }

        @Override
        public void recieve(SortMessage msg, ActorRef<SortMessage> sender, MessageActor<SortMessage> self) {
            SortMessage.Type type = msg.type;
            switch (type) {
                case REQUEST:
                    request(msg, sender, self);
                    break;
                case REPLY:
                    reply(msg.value, msg.side, self);
                    break;
            }
        }

    }

}
