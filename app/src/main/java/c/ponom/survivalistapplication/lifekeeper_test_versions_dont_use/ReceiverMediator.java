package c.ponom.survivalistapplication.lifekeeper_test_versions_dont_use;

public class ReceiverMediator {

    KeepAliveReceiver receiver;
     ReceiverMediator() {

    }

    synchronized  void initReceiver(KeepAliveReceiver  keepAliveReceiver) {
        receiver = keepAliveReceiver;
    }



}
