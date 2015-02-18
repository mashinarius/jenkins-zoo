package com.mashinarius.jenkinszoo.jenkins;

import com.mashinarius.jenkinszoo.commons.Constants;
import com.mashinarius.jenkinszoo.zoo.ConnectionUtils;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import java.io.IOException;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

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

	/**
	 * We'll use this from the <tt>config.jelly</tt>.
	 */
	public String getNodePath()
	{
		return nodePath;
	}

    public String getZooValue()
    {
        return zooValue;
    }

    /*@Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();*/
	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener)
	{
		// This is where you 'build' the project.
		// Since this is a dummy, we just say 'hello world' and call that a
		// build.

		// This also shows how you can consult the global configuration of the
		// builder

        ConnectionUtils example = new ConnectionUtils();

        try {
            example.setConnectionUrl(getDescriptor().getZoo_hostname() + ":" + getDescriptor().getZoo_port());
            example.connect();

            if (nodePath.length()>0 && zooValue.length()>0)
            {
                example.setStringData(nodePath,zooValue);
                System.out.println(example.getStringData(nodePath));
                listener.getLogger().println(example.getStringData(nodePath));
            }
            else
            {
                System.out.println(example.getStringData(Constants.SDB_TEST1_DATABASE));
                listener.getLogger().println(example.getStringData(Constants.SDB_TEST1_DATABASE));
            }

            example.disconnect();
        } catch (IOException e) {

            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
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

	public FormValidation doTestZooConnection(@QueryParameter("zoo_hostname") final String hostname, @QueryParameter("zoo_port") final Integer port,
			@QueryParameter("zoo_username") final String username, @QueryParameter("zoo_password") final String password) throws IOException, ServletException
	{
		String connectString = hostname + ":" + port;
        if (ConnectionUtils.testConnection(connectString))
		    return FormValidation.ok("Success ");
        else
            return FormValidation.error("Connection error");
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
		private boolean useFrench;
        private String zoo_hostname;
        private String zoo_port;
        private String zoo_node;

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
			useFrench = formData.getBoolean("useFrench");
            zoo_hostname = formData.getString("zoo_hostname");
            zoo_port = formData.getString("zoo_port");
            zoo_node = formData.getString("zoo_node");
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
		public boolean getUseFrench()
		{
			return useFrench;
		}

        public String getZoo_hostname()
        {
            return zoo_hostname;
        }
        public String getZoo_port()
        {
            return zoo_port;
        }

        public String getZoo_node()
        {
            return zoo_node;
        }
	}
}
