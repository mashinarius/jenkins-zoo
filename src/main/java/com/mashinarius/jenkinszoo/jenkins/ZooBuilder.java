package com.mashinarius.jenkinszoo.jenkins;

import com.mashinarius.jenkinszoo.commons.Constants;
import com.mashinarius.jenkinszoo.zoo.ConnectionUtils;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;

public class ZooBuilder extends Builder
{

	private final String nodePath;
    private final String zooValue;

	@DataBoundConstructor
	public ZooBuilder(String nodePath, String zooValue)
	{
		this.nodePath = nodePath;
        this.zooValue = zooValue;
	}

	public String getNodePath()
	{
		return nodePath;
	}

    public String getZooValue()
    {
        return zooValue;
    }


    /**
     * This is where you 'build' the project.
     * @param build
     * @param launcher
     * @param listener
     * @return
     */
	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener)
	{
        ConnectionUtils example = new ConnectionUtils(getDescriptor().getZooHostname(),getDescriptor().getZooPort(),getDescriptor().getZooTimeout());
            //example.connect();

            if (nodePath.length()>0 && zooValue.length()>0)
            {
                example.setStringData(nodePath,zooValue);
                String result = example.getStringData(nodePath);
                System.out.println(result);
                listener.getLogger().println(result);
            }
            else
            {
                String result = example.getStringData(Constants.SDB_TEST1_DATABASE);
                System.out.println(result);
                listener.getLogger().println(result);
            }

		return true;
	}

	// Overridden for better type safety.
	// If your plugin doesn't really define any property on Descriptor,
	// you don't have to do this.
	@Override
	public DescriptorImpl getDescriptor()
	{
		return (DescriptorImpl) super.getDescriptor();
	}



	/**
	 * Descriptor for {@link ZooBuilder}. Used as a singleton. The class is
	 * marked as public so that it can be accessed from views.
	 * <p/>
	 * <p/>
	 * See
	 * <tt>src/main/resources/hudson/plugins/hello_world/ZooBuilder/*.jelly</tt>
	 * for the actual HTML fragment for the configuration screen.
	 */
	@Extension
	// This indicates to Jenkins that this is an implementation of an extension
	// point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder>
	{
		/**
		 * To persist global configuration information, simply store it in a
		 * field and call save().
		 * <p/>
		 * <p/>
		 * If you don't want fields to be persisted, use <tt>transient</tt>.
		 */
        private String zooHostname;
        private Integer zooPort;
        private String zooNode;
        private Integer zooTimeout;
        private String zooUsername;
        private String zooPassword;
        /**
         *  Test Connections to the ZooKeeper server from the Global config page
         * @param hostname
         * @param port
         * @param timeout
         * @param username
         * @param password
         * @return
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doTestConnection(
                @QueryParameter("zooHostname") final String hostname,
                @QueryParameter("zooPort") final Integer port,
                @QueryParameter("zooTimeout") final Integer timeout,
                @QueryParameter("zooNode") final String node,
                @QueryParameter("zooUsername") final String username,
                @QueryParameter("zooPassword") final String password) throws IOException, ServletException
        {
            String connectString = hostname + ":" + port;
            if (ConnectionUtils.testConnection(hostname, port, timeout))
                return FormValidation.ok("Success ");
            else
                return FormValidation.error("Connection error");
        }
		/**
		 * In order to load the persisted global configuration, you have to call
		 * load() in the constructor.
		 */
		public DescriptorImpl()
		{
			load();
		}

		/**
		 * Performs on-the-fly validation of the form field 'name'.
		 *
		 * @param value
		 *            This parameter receives the value that the user has typed.
		 * @return Indicates the outcome of the validation. This is sent to the
		 *         browser.
		 *         <p/>
		 *         Note that returning {@link FormValidation#error(String)} does
		 *         not prevent the form from being saved. It just means that a
		 *         message will be displayed to the user.
		 */
		public FormValidation doCheckName(@QueryParameter String value) throws IOException, ServletException
		{
			if (value.length() == 0)
				return FormValidation.error("Please set a name");
			if (value.length() < 4)
				return FormValidation.warning("Isn't the name too short?");
			return FormValidation.ok();
		}

		public boolean isApplicable(Class<? extends AbstractProject> aClass)
		{
			// Indicates that this builder can be used with all kinds of project
			// types
			return true;
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName()
		{
			return "Run Zoo Test";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException
		{
			// To persist global configuration information,
			// set that to properties and call save().
            zooHostname = formData.getString("zooHostname");
            zooPort = formData.getInt("zooPort");
            zooTimeout = formData.getInt("zooTimeout");
            zooNode = formData.getString("zooNode");
            zooUsername = formData.getString("zooUsername");
            zooPassword = formData.getString("zooPassword");
			// ^Can also use req.bindJSON(this, formData);
			// (easier when there are many fields; need set* methods for this,
			// like setUseFrench)

            save();
			return super.configure(req, formData);
		}

		/**
		 * This method returns true if the global configuration says we should
		 * speak French.
		 * <p/>
		 * The method name is bit awkward because global.jelly calls this method
		 * to determine the initial state of the checkbox by the naming
		 * convention.
		 */
        public String getZooHostname()
        {
            return zooHostname;
        }
        public Integer getZooPort()
        {
            return zooPort;
        }

        public String getZooNode()
        {
            return zooNode;
        }

        public Integer getZooTimeout()
        {
            return zooTimeout;
        }

        public String getZooUsername() {
            return zooUsername;
        }
        public String getZooPassword() {
            return zooPassword;
        }
	}
}
