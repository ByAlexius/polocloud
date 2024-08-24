package dev.httpmarco.polocloud.node.commands;

import dev.httpmarco.polocloud.node.commands.type.KeywordArgument;
import dev.httpmarco.polocloud.node.commands.type.StringArrayArgument;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Log4j2
@UtilityClass
public class CommandParser {

    public void serializer(@NotNull CommandService commandService, String name, String[] args) {
        // all command with same start name
        List<Command> commands = commandService.commandsByName(name);

        if (executeCommand(commands, args)) {
            return;
        }

        // we must calculate the usage, because no command was found

        for (var command : commands) {

            if (command.defaultExecution() != null) {
                command.defaultExecution().execute(new CommandContext());
            }  else {
                for (var syntaxCommand : command.commandSyntaxes()) {
                    log.info("{} {}", command.name(), syntaxCommand.usage());
                }
            }
        }
    }

    private boolean executeCommand(@NotNull List<Command> commands, String[] args) {
        for (var command : commands) {

            if ((!command.hasSyntaxCommands()) || args.length == 0) {
                return false;
            }

            for (var syntaxCommand : command.commandSyntaxes()) {

                if (args.length != syntaxCommand.arguments().length && Arrays.stream(syntaxCommand.arguments()).noneMatch(it -> it instanceof StringArrayArgument)) {
                    continue;
                }

                var commandContext = new CommandContext();

                var provedSyntax = true;
                var provedSyntaxWarning = Optional.empty();

                for (int i = 0; i < syntaxCommand.arguments().length; i++) {
                    var argument = syntaxCommand.arguments()[i];
                    var rawInput = args[i];

                    if(argument instanceof StringArrayArgument arrayArgument) {
                        commandContext.append(arrayArgument, argument.buildResult(String.join(" ", Arrays.copyOfRange(args, i, args.length))));
                        break;
                    }else if (argument instanceof KeywordArgument keywordArgument) {
                        if (!keywordArgument.key().equalsIgnoreCase(rawInput)) {
                            provedSyntax = false;
                            break;
                        }
                    } else if (!argument.predication(rawInput)) {
                        provedSyntaxWarning = Optional.of(argument.wrongReason());
                        continue;
                    }

                    commandContext.append(argument, argument.buildResult(rawInput));
                }

                if (!provedSyntax) {
                    continue;
                }

                if (provedSyntaxWarning.isPresent()) {
                    log.warn(provedSyntaxWarning.get());
                    return true;
                }

                syntaxCommand.execution().execute(commandContext);
                return true;
            }
        }
        return false;
    }
}