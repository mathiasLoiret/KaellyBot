package commands;

import enums.Language;
import util.Message;
import exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.obj.IMessage;
import util.Translator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Created by steve on 14/07/2016.
 */
public class AvailableCommand extends AbstractCommand{

    private final static Logger LOG = LoggerFactory.getLogger(AvailableCommand.class);
    private DiscordException tooMuchCmds;
    private DiscordException notFoundCmd;
    private DiscordException forbiddenCmdFound;
    private DiscordException forbiddenCmdNotFound;

    public AvailableCommand(){
        super("available","\\s+(\\w+)\\s+(on|off|0|1|true|false)");
        setAdmin(true);
        tooMuchCmds = new TooMuchDiscordException("exception.toomuch.cmds", "exception.toomuch.cmds_found");
        notFoundCmd = new NotFoundDiscordException("exception.notfound.cmd", "exception.notfound.cmd_found");
        forbiddenCmdFound = new BasicDiscordException("exception.basic.forbidden_command_found");
        forbiddenCmdNotFound = new BasicDiscordException("exception.basic.forbidden_command_notfound");
    }

    @Override
    public boolean request(IMessage message) {
        if (super.request(message)) {
            Matcher m = getMatcher(message);
            m.find();
            Language lg = Translator.getLanguageFrom(message.getChannel());
            List<Command> potentialCmds = new ArrayList<>();
            String commandName = m.group(1).trim();
            for (Command command : CommandManager.getCommands())
                if (!command.isAdmin() && command.getName().contains(commandName))
                    potentialCmds.add(command);

            if (potentialCmds.size() == 1){
                Command command = potentialCmds.get(0);
                String value = m.group(2);

                if (command instanceof AvailableCommand || command instanceof AboutCommand){
                    Message.sendText(message.getChannel(), Translator.getLabel(lg, "announce.request.1"));
                    return false;
                }
                if (value.matches("false") || value.matches("1") || value.matches("off")){
                    if (command.isPublic()) {
                        command.setPublic(false);
                        Message.sendText(message.getChannel(), Translator.getLabel(lg, "announce.request.2") + " *" + commandName
                                + "* " + Translator.getLabel(lg, "announce.request.3"));
                    }
                    else
                        forbiddenCmdFound.throwException(message, this, lg);
                }
                else if (value.matches("true") || value.matches("0") || value.matches("on")){
                    if (! command.isPublic()) {
                        command.setPublic(true);
                        Message.sendText(message.getChannel(), Translator.getLabel(lg, "announce.request.2") + "*" + commandName
                                + "* " + Translator.getLabel(lg, "announce.request.4"));
                    }
                    else
                        forbiddenCmdNotFound.throwException(message, this, lg);
                }
                else
                    new BadUseCommandDiscordException().throwException(message, this, lg);
            }
            else if (potentialCmds.isEmpty())
                notFoundCmd.throwException(message, this, lg);
            else
                tooMuchCmds.throwException(message, this, lg);
        }
        return false;
    }

    @Override
    public String help(Language lg, String prefixe) {
        return "**" + prefixe + name + "** " + Translator.getLabel(lg, "available.help");
    }

    @Override
    public String helpDetailed(Language lg, String prefixe) {
        return help(lg, prefixe)
                + "\n" + prefixe + "`"  + name + "*CommandForbidden* true` : " + Translator.getLabel(lg, "available.detailed.1")
                + "\n" + prefixe + "`"  + name + "*CommandForbidden* false` : " + Translator.getLabel(lg, "available.detailed.2") + "\n";
    }
}
