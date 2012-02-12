package atg.bsh;

import java.util.*;
import java.text.MessageFormat;

import javax.servlet.Servlet;

import bsh.Interpreter;

import atg.nucleus.GenericService;
import atg.nucleus.Nucleus;
import atg.nucleus.ServiceAdminServlet;

/**
 * A very simple service that serves as a placeholder for the bsh admin servlet.
 *
 * @author wsargent
 * @since Feb 9, 2006
 * @version $Revision$ 
 *
 */
public class BshAdminService extends GenericService
{
    private Servlet mServlet;
    
    /* (non-Javadoc)
     * @see atg.nucleus.GenericService#createAdminServlet()
     */
    protected Servlet createAdminServlet()
    {
        // This may not be thread-safe...
        if (mServlet == null) 
        {
            Nucleus nuke = Nucleus.getGlobalNucleus();            
            mServlet = new BshAdminServlet(this, nuke) {
                public void setInterpreterParameters(Interpreter pInterpreter)
                {
                    // Set your own properties here, i.e.
                    // setRepository("repository", mRepository);
                }
            };
        }
        return mServlet;
    }

}
