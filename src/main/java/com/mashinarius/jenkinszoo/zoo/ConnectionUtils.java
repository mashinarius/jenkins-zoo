package com.mashinarius.jenkinszoo.zoo;

import com.mashinarius.jenkinszoo.commons.Constants;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.net.ConnectException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionUtils implements Constants {
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

    private boolean checkExist(String nodePath)
    {
        boolean  value = false;
        ZooKeeper zkk = null;
        try {
            zkk = connect();
            value = zkk.exists(nodePath, false) != null;
            zkk.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        } finally {
            if (zkk!=null)
            {
                try {
                    zkk.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

    }

        return value;
    }

    private  static int nthOccurrence(String str, char c, int n) {
        int pos = str.indexOf(c, 0);
        while (n-- > 0 && pos != -1)
            pos = str.indexOf(c, pos+1);
        return pos;
    }

    public void createAllNodes(String nodePath)
    {
        for (int i = 0; i < StringUtils.countMatches(nodePath, SLASH) ;i++)
        {
            int slashCount = nthOccurrence(nodePath, '/', i);
            String parent;
            if (nodePath.lastIndexOf(SLASH) == slashCount)
            {
                parent = nodePath;
            }
            else
            {
                parent = nodePath.substring(0, nthOccurrence(nodePath, '/', i));
            }

            if (!checkExist(parent))
            {
                createNode(parent);
            }

        }
/*        if (!checkExist(nodePath))
        {
            String result = getZooKeeperInstance().create(nodePath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        while(nodePath.contains(SLASH) && nodePath.lastIndexOf(SLASH)!=nodePath.indexOf(SLASH))
        {
            String parentPath = nodePath.substring(nodePath.indexOf(SLASH), nodePath.indexOf(SLASH, 1));
        }
        return false;*/
    }
    /**
     * Try to connect (3 times with 20ms interval)
     * @param host
     * @param port
     * @param sessionTimeout
     * @return
     */
    public static boolean testConnection(String host, Integer port, Integer sessionTimeout) {
        ZooKeeper zk = null;
        try {
             zk = new ConnectionUtils(host, port, sessionTimeout).connect();
            for(int i=0; i < 3; i++)
            {
                if (zk.getState().equals(States.CONNECTED)) {
                    return true;
                }
                else if (zk.getState().equals(States.CONNECTING)) {
                    Thread.sleep(20);
                }
                else {
                    return false;
                }
            }
        } catch (ConnectException e ) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (zk!=null)
            {
                try {
                    zk.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public static String getConnectionUrl(String host, String port)
    {
        if (host.contains(":"))
            return host;
            else
            return new String (host + ":" + port);
    }

/*    private ZooKeeper getZooKeeperInstance() throws IOException, InterruptedException {
        if (zk != null && zk.getState().equals(States.CONNECTED)) {
            return zk;
        } else {
            return connect();
        }
    }*/
    private ZooKeeper connect() throws IOException, InterruptedException {
        final CountDownLatch connectedSignal = new CountDownLatch(1);
        //TODO Tests
        ZooKeeper zk = new ZooKeeper(connectionString, timeout, new Watcher() {
            //@Override
            public void process(WatchedEvent event) {
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    connectedSignal.countDown();
                }
            }
        });
        connectedSignal.await(AWAIT_TIMEOUT, TimeUnit.MILLISECONDS);
        //connectedSignal.await();
        return zk;
    }

    public void createNode(String nodePath) {
        try {
            ZooKeeper zkk = connect();
            if (zkk.exists(nodePath, false) == null)
            {
                System.out.printf("Creating top level znode %s for transaction examples\n", nodePath);
                zkk.create(nodePath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                zkk.close();
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

            if (!checkExist(nodePath))
            {
                createAllNodes(nodePath);
            }

            ZooKeeper zkk = connect();
            if (null != zkk.getData(nodePath, false, stat)) {
                System.out.println("Path allready exist ; " + new String(zkk.getData(nodePath, false, stat)));
            }


            stat = zkk.setData(nodePath, value.getBytes(), stat.getVersion());

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(stat.getMtime());

            Date date = new Date(stat.getMtime());
            DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
            String dateFormatted = formatter.format(date);
            System.out.println("Updated : " + dateFormatted);

            zkk.close();

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
            ZooKeeper zkk = connect();
            value = new String(zkk.getData(nodePath, false, st1));
            zkk.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
        return value;
    }
}
