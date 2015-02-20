package com.mashinarius.jenkinszoo.zoo;

import com.mashinarius.jenkinszoo.commons.Constants;
import org.apache.zookeeper.*;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.net.ConnectException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ConnectionUtils implements Constants {
    private static ZooKeeper zk;
    /*private String host;
    private Integer port;*/

    private String connectionString;
    private Integer timeout;
    private ConnectionUtils () {}

    public ConnectionUtils (String hosts)
    {
        if (hosts.contains(":"))
        {
            this.connectionString = hosts;
        }
        else
        {
            this.connectionString = hosts + ":" + Constants.PORT;
        }
        this.timeout = Constants.SESSION_TIMEOUT;
    }
    public ConnectionUtils (String hosts, Integer port)
    {
        this.connectionString = hosts + ":" + port;
        this.timeout = Constants.SESSION_TIMEOUT;
    }
    public ConnectionUtils (String hosts, Integer port, Integer timeout)
    {
        this.connectionString = hosts + ":" + port;
        this.timeout = timeout;
    }

    public static boolean testConnection(String host, Integer port, Integer timeout) {
        try {
            zk = new ConnectionUtils(host, port, timeout).connect();
            int i = 3;
            while (i > 0 && zk != null && (ZooKeeper.States.CONNECTING == zk.getState() || States.CONNECTED == zk.getState()))
            {
                if (zk.getState().equals(States.CONNECTED)) {
                    return true;
                }
                Thread.sleep(20);
                i--;

            }
            zk.close();
        } catch (ConnectException e ) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ZooKeeper getZooKeeperInstance() throws IOException, InterruptedException {
        if (zk != null && zk.getState().equals(States.CONNECTED)) {
            return zk;
        } else {
            return connect();
        }
    }
    public ZooKeeper connect() throws IOException, InterruptedException {
        final CountDownLatch connectedSignal = new CountDownLatch(1);

        //TODO Tests
/*        if (this.host.contains(":") && this.host.contains(",")) // multiple server configuration
        {
            connectionString = new String(this.host);
        }
        else
        {
            connectionString = new String(this.host+":"+this.port);
        }*/

        zk = new ZooKeeper(connectionString, timeout, new Watcher() {
            //@Override
            public void process(WatchedEvent event) {
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    connectedSignal.countDown();
                }
            }
        });
        connectedSignal.await(SESSION_TIMEOUT * 2, TimeUnit.MILLISECONDS);
        return zk;
    }

    public void createNode(String nodePath) {

        try {
            if (getZooKeeperInstance().exists(nodePath, false) == null) {
                System.out.printf("Creating top level znode %s for transaction examples\n", nodePath);
                getZooKeeperInstance().create(nodePath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                getZooKeeperInstance().close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    public void setStringData(String nodePath, String value) {
        try {
            Stat stat = new Stat();

            if (null != getZooKeeperInstance().getData(nodePath, false, stat)) {
                System.out.println("Path allready exist ; " + new String(getZooKeeperInstance().getData(nodePath, false, stat)));
            }

            stat = getZooKeeperInstance().setData(nodePath, value.getBytes(), stat.getVersion());

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(stat.getMtime());

            Date date = new Date(stat.getMtime());
            DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
            String dateFormatted = formatter.format(date);
            System.out.println("Updated : " + dateFormatted);

            getZooKeeperInstance().close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    public String getStringData(String nodePath) {
        Stat st1 = new Stat();
        String value = null;
        try {
            value = new String(getZooKeeperInstance().getData(nodePath, false, st1));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
        return value;
    }

    public void disconnect() {
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
