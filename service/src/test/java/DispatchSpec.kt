import com.relay.service.Channels
import com.relay.service.RelayService
import org.junit.Test

/**
 * @author jasonflax on 4/2/16.
 */
class DispatchSpec {
    init {
        RelayService.init("http://localhost:9000")
    }

    @Test
    fun dispatch_TestValidity() {
        println(Channels.get().parcel)
    }
}
