package com.github.Ringoame196

import com.sun.net.httpserver.HttpServer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import java.io.File
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL

class WebServer(private val plugin: Plugin) {
    object DataManager {
        var webServer: HttpServer? = null
    }
    private val port = plugin.config.getInt("serverPort")
    private val htmlFile = File(plugin.dataFolder, "index.html")
    fun start(commandSender: CommandSender) {
        if (DataManager.webServer != null) {
            commandSender.sendMessage("${ChatColor.RED}既にサーバーは公開されています")
            return
        }
        val server = HttpServer.create(InetSocketAddress(port), 0)

        if (checkOpenPort()) {
            commandSender.sendMessage("${ChatColor.RED}${port}番は既に使われているため公開されませんでした")
            return
        }

        server.createContext("/") { exchange ->
            try {
                val htmlContent = htmlFile.readText()
                // Content-Typeを設定する
                exchange.responseHeaders.set("Content-Type", "text/html; charset=UTF-8")
                exchange.sendResponseHeaders(200, htmlContent.length.toLong())
                val responseBody = exchange.responseBody
                responseBody.write(htmlContent.toByteArray())
                responseBody.close()
                DataManager.webServer = server
                Bukkit.broadcastMessage("${ChatColor.YELLOW}[Webサーバー] ${commandSender.name}がWebサーバーを公開しました")
                commandSender.sendMessage("${ChatColor.AQUA}${port}番ポートで公開しました")
            } catch (e: Exception) {
                exchange.sendResponseHeaders(500, 0)
                commandSender.sendMessage("${ChatColor.RED}公開中にエラーが発生しました　詳細なエラーはコンソールを確認してください")
                Bukkit.getConsoleSender().sendMessage(e.message)
                server.stop(0)
                return@createContext
            }
        }

        server.start()
        check(commandSender)
    }
    fun check(commandSender: CommandSender) {
        if (checkOpenPort()) {
            commandSender.sendMessage("${ChatColor.RED}[Webサーバーチェック] 公開されていません")
            return
        }
        if (DataManager.webServer == null) {
            commandSender.sendMessage("${ChatColor.RED}[Webサーバーチェック] 別サーバーが動いています")
            return
        }
        try {
            val url = URL("http://localhost:$port")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val inputStream = connection.inputStream
                val htmlFromServer = inputStream.bufferedReader().use { it.readText() }
                val expectedHtml = htmlFile.readText()

                if (htmlFromServer == expectedHtml) {
                    commandSender.sendMessage("${ChatColor.YELLOW}[Webサーバーチェック] 正常に公開されています")
                } else {
                    commandSender.sendMessage("${ChatColor.YELLOW}[Webサーバーチェック] サーバー自体は公開されていますが、HTMLが正常に反映されていません")
                    commandSender.sendMessage("${ChatColor.RED}HTMLファイル内に日本語や特殊文字が含まれているなど HTMLファイルが正常に作られていない可能性があります")
                }
            } else {
                commandSender.sendMessage("${ChatColor.RED}サーバー公開されていません。レスポンスコード: $responseCode")
            }

            connection.disconnect()
        } catch (e: ConnectException) {
            // 接続が拒否された場合の処理
            commandSender.sendMessage("${ChatColor.RED}サーバーに接続できませんでした サーバーが起動していない可能性があります")
        } catch (e: Exception) {
            // その他の例外の処理
            commandSender.sendMessage("${ChatColor.RED}エラーが発生しました 詳細なエラーはコンソールを確認してください")
            Bukkit.getConsoleSender().sendMessage(e.message)
            e.printStackTrace()
        }
    }
    private fun checkOpenPort(): Boolean {
        var socket: Socket? = null
        return try {
            // ソケットを作成して接続を試みる
            socket = Socket("localhost", port)
            // 接続が成功した場合はポートが使用されている
            false
        } catch (e: Exception) {
            // 接続に失敗した場合はポートが空いている
            true
        } finally {
            // ソケットをクローズする
            socket?.close()
        }
    }

    fun stop(commandSender: CommandSender) {
        val webServer = DataManager.webServer
        try {
            if (webServer == null) {
                commandSender.sendMessage("${ChatColor.RED}サーバー起動していません")
                return
            }
            webServer.stop(0)
            Bukkit.broadcastMessage("${ChatColor.RED}[Webサーバー]${commandSender.name}がWebサーバーを停止しました")
            DataManager.webServer = null
        } catch (e: Exception) {
            commandSender.sendMessage("${ChatColor.RED}サーバー停止中にエラーが発生しました 詳細なエラーはコンソールを確認してください")
            Bukkit.getConsoleSender().sendMessage(e.message)
        }
    }
}
