package fr.azodox.gtb.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import fr.azodox.gtb.lang.LanguageCore
import org.bukkit.entity.Player

@CommandAlias("language")
class LanguageCommand(private val languageCore: LanguageCore) : BaseCommand() {

    @Default
    @Subcommand("set")
    @Syntax("<language>")
    @CommandCompletion("@locales")
    fun setLocale(player: Player, locale: String) {
        languageCore.setLocale(player.uniqueId, locale)
        player.sendMessage(LanguageCore.languages[locale]!!.format("language.set", locale))
    }
}