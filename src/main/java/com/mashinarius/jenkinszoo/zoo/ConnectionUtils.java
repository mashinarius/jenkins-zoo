package com.mashinarius.jenkinszoo.zoo;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import com.mashinarius.jenkinszoo.commons.Constants;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.Stat;

public class ConnectionUtils implements Constants
{
	private static ZooKeeper zk;

    public static boolean testConnection(String connectionString)
    {
        try {
            zk = connect(connectionString, SESSION_TIMEOUT);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (zk !=null && ZooKeeper.States.CONNECTING == zk.getState())
        {
            try
            {
                if (zk.getState().equals(States.CONNECTED))
                {
                    return true;
                }
                Thread.sleep(20);
            } catch (InterruptedException e)
            {
            }
        }
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

	private ZooKeeper getZK() throws IOException, InterruptedException
	{
		if (zk != null && zk.getState().equals(States.CONNECTED))
		{
			return zk;
		} else
		{
			return connect();
		}
	}

	private static ZooKeeper connect(String hosts, int sessionTimeout) throws IOException, InterruptedException
	{

		final CountDownLatch connectedSignal = new CountDownLatch(1);
		zk = new ZooKeeper(hosts, sessionTimeout, new Watcher()
		{
			//@Override
			public void process(WatchedEvent event)
			{
				if (event.getState() == Event.KeeperState.SyncConnected)
				{
					connectedSignal.countDown();
				}
			}
		});
		connectedSignal.await();
		return zk;
	}

	public ZooKeeper connect() throws IOException, InterruptedException
	{
		return connect(Constants.CONNECTION, Constants.SESSION_TIMEOUT);
	}

	public void createNode(Object value, String nodePath)
	{

		try
		{
			if (getZK().exists(nodePath, false) == null)
			{
				System.out.printf("Creating top level znode %s for transaction examples\n", nodePath);
				getZK().create(nodePath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeeperException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setStringData(String nodePath, String value)
	{
		try
		{
			Stat stat = new Stat();

			if (null != getZK().getData(nodePath, false, stat))
			{
				System.out.println("Path allready exist ; " + new String(getZK().getData(nodePath, false, stat)));
			}

			stat = getZK().setData(nodePath, value.getBytes(), stat.getVersion());

			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(stat.getMtime());

			Date date = new Date(stat.getMtime());
			DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
			String dateFormatted = formatter.format(date);
			System.out.println("Updated : " + dateFormatted);

			getZK().close();

		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		} catch (KeeperException e)
		{
			e.printStackTrace();
		}
	}

	public String getStringData(String nodePath)
	{
		Stat st1 = new Stat();
		String value = null;
		try
		{
			value = new String(getZK().getData(nodePath, false, st1));
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeeperException e)
		{
			// TODO Auto-generated catch block
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
