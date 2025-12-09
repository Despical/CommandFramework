/**
 * @author Despical
 * <p>
 * Created at 9.12.2025
 */
module dev.despical.commandframework {

    requires static transitive org.jetbrains.annotations;

    requires org.bukkit;
    requires java.logging;
    requires com.google.common;
    requires net.kyori.adventure;
    requires net.kyori.examination.api;
    requires org.checkerframework.checker.qual;

    exports dev.despical.commandframework;
    exports dev.despical.commandframework.annotations;
    exports dev.despical.commandframework.debug;
    exports dev.despical.commandframework.exceptions;
    exports dev.despical.commandframework.options;
}
