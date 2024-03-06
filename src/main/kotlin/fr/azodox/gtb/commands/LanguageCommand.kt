package fr.azodox.gtb.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import fr.azodox.gtb.lang.LanguageCore
import fr.azodox.gtb.lang.language
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

private const val LANGUAGE_COMMAND_SET = "language.command.set"

private const val LANGUAGE_COMMAND_RELOAD = "language.command.reload"

private const val LANGUAGE_COMMAND_HELP = "language.command.help"

@CommandAlias("language")
class LanguageCommand(private val languageCore: LanguageCore) : BaseCommand() {

    @Subcommand("set")
    @Syntax("<language>")
    @CommandCompletion("@locales")
    fun setLocale(player: Player, locale: String) {
        languageCore.setLocale(player.uniqueId, locale)
        player.sendMessage(LanguageCore.languages[locale]!!.format(LANGUAGE_COMMAND_SET, locale))
    }

    @Subcommand("reload")
    @Description("Refresh the messages in cache from the locale files")
    fun reload(sender: CommandSender){
        languageCore.reload()
        if (sender is Player) {
            sender.sendMessage(language(sender).format(LANGUAGE_COMMAND_RELOAD))
        }else{
            sender.sendMessage("Language files reloaded")
        }
    }

    @Default
    @HelpCommand
    fun onHelp(player: Player) {
        player.sendMessage(
            language(player).format(LANGUAGE_COMMAND_HELP, LanguageCore.languages.keys.joinToString(", "))
        )
    }
}
