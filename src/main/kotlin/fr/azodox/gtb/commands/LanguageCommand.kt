package fr.azodox.gtb.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import fr.azodox.gtb.lang.LanguageCore
import fr.azodox.gtb.lang.language
import org.bukkit.entity.Player

@CommandAlias("language")
class LanguageCommand(private val languageCore: LanguageCore) : BaseCommand() {

    @Subcommand("set")
    @Syntax("<language>")
    @CommandCompletion("@locales")
    fun setLocale(player: Player, locale: String) {
        languageCore.setLocale(player.uniqueId, locale)
        player.sendMessage(LanguageCore.languages[locale]!!.format("language.command.set", locale))
    }

    @Default
    @HelpCommand
    fun onHelp(player: Player) {
        player.sendMessage(
            language(player).format("language.command.help", LanguageCore.languages.keys.joinToString(", "))
        )
    }
}
