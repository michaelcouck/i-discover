package discover.tool;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 12-10-2010
 */
public final class URI {

    protected static final Logger LOGGER = Logger.getLogger(URI.class);

    /**
     * Singularity.
     */
    private URI() {
        // Documented
    }

    /**
     * Accepted protocols.
     */
    protected static final Pattern PROTOCOL_PATTERN;
    /**
     * The pattern regular expression to match a url.
     */
    protected static final Pattern EXCLUDED_PATTERN;
    /**
     * The pattern to strip the JSessionId form the urls.
     */
    protected static final Pattern JSESSIONID_PATTERN;
    /**
     * The anchor pattern.
     */
    protected static final Pattern ANCHOR_PATTERN;
    /**
     * The carriage return/line feed pattern.
     */
    protected static final Pattern CARRIAGE_LINE_FEED_PATTERN;
    /**
     * The pattern for ip addresses, i.e. 192.168.1.0 etc.
     */
    protected static final Pattern IP_PATTERN;

    static {
        ANCHOR_PATTERN = Pattern.compile("#[^#]*$");
        CARRIAGE_LINE_FEED_PATTERN = Pattern.compile("[\n\r]");
        PROTOCOL_PATTERN = Pattern.compile("(http).*|(www).*|(https).*|(ftp).*");
        EXCLUDED_PATTERN = Pattern.compile("^news.*|^javascript.*|^mailto.*|^plugintest.*|^skype.*");
        JSESSIONID_PATTERN = Pattern.compile("([;_]?((?i)l|j|bv_)?((?i)sid|phpsessid|sessionid)=.*?)(\\?|&amp;|#|$)");
        IP_PATTERN = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.)" +
                "{3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
    }

    /**
     * Resolves a URI reference against a base URI. Work-around for bug in java.net.URI
     * (<http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4708535>)
     *
     * @param baseURI   the base URI
     * @param reference the URI reference
     * @return the resulting URI
     */
    public static String resolve(final java.net.URI baseURI, final String reference) {
        StringBuilder builder = new StringBuilder();
        // Strip the space characters from the reference
        String trimmedReference = URI.stripBlanks(reference);
        URL resolved = null;
        try {
            resolved = new URL(baseURI.toURL(), trimmedReference);
        } catch (MalformedURLException e) {
            LOGGER.error("Exception resolving url : " + baseURI + ", " + trimmedReference, e);
        }
        builder.append(resolved);
        return builder.toString();
    }

    /**
     * Resolves a URI reference against a base URI. Work-around for bugs in java.net.URI (e.g.
     * <http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4708535>)
     *
     * @param baseURI   the base URI
     * @param reference the URI reference
     * @return the resulting URI
     */
    public static java.net.URI resolve(final java.net.URI baseURI, java.net.URI reference) {
        if (baseURI == null) {
            throw new IllegalArgumentException("Base URI may not be null");
        }
        if (reference == null) {
            throw new IllegalArgumentException("Reference URI may not be null");
        }
        String s = reference.toString();
        if (s == null || s.length() == 0) {
            return baseURI;
        }
        if (s.charAt(0) == '?') {
            return resolveReferenceStartingWithQueryString(baseURI, reference);
        }
        java.net.URI newReference = reference;
        boolean emptyReference = s.length() == 0;
        if (emptyReference) {
            newReference = java.net.URI.create("#");
        }
        java.net.URI resolved = baseURI.resolve(newReference);
        if (emptyReference) {
            String resolvedString = resolved.toString();
            resolved = java.net.URI.create(resolvedString.substring(0, resolvedString.indexOf('#')));
        }
        return removeDotSegments(resolved);
    }

    /**
     * Removes dot segments according to RFC 3986, section 5.2.4
     *
     * @param uri the original URI
     * @return the URI without dot segments
     */
    public static java.net.URI removeDotSegments(final java.net.URI uri) {
        String path = uri.getPath();
        if ((path == null) || (!path.contains("/."))) {
            // No dot segments to remove
            return uri;
        }
        StringBuilder outputBuffer = removeDotSegments(path);
        try {
            return new java.net.URI(uri.getScheme(), uri.getAuthority(), outputBuffer.toString(), uri.getQuery(),
                    uri.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static StringBuilder removeDotSegments(String path) {
        String[] inputSegments = path.split("/");
        Stack<String> outputSegments = new Stack<String>();
        for (final String inputSegment : inputSegments) {
            if ("..".equals(inputSegment)) {
                if (!outputSegments.isEmpty()) {
                    outputSegments.pop();
                }
            } else {
                if (!".".equals(inputSegment)) {
                    outputSegments.push(inputSegment);
                }
            }
        }
        StringBuilder outputBuffer = new StringBuilder();
        boolean first = Boolean.TRUE;
        for (final String outputSegment : outputSegments) {
            if (!first) {
                outputBuffer.append('/');
            }
            first = Boolean.FALSE;
            outputBuffer.append(outputSegment);
        }
        return outputBuffer;
    }

    /**
     * Resolves a reference starting with a query string.
     *
     * @param baseURI   the base URI
     * @param reference the URI reference starting with a query string
     * @return the resulting URI
     */
    private static java.net.URI resolveReferenceStartingWithQueryString(final java.net.URI baseURI, final java.net.URI reference) {
        String baseUri = baseURI.toString();
        baseUri = baseUri.indexOf('?') > -1 ? baseUri.substring(0, baseUri.indexOf('?')) : baseUri;
        return java.net.URI.create(baseUri + reference.toString());
    }

    public static boolean isExcluded(final String string) {
        if (string == null) {
            return Boolean.TRUE;
        }
        // Blank link is useless
        if ("".equals(string)) {
            return Boolean.TRUE;
        }
        // Check that there is at least one character in the link
        char[] chars = string.toCharArray();
        boolean containsCharacters = Boolean.FALSE;
        for (char c : chars) {
            if (Character.isLetterOrDigit(c)) {
                containsCharacters = Boolean.TRUE;
                break;
            }
        }
        if (!containsCharacters) {
            return Boolean.TRUE;
        }
        String lowerCaseString = string.toLowerCase(Locale.getDefault());
        return EXCLUDED_PATTERN.matcher(lowerCaseString).matches();
    }

    public static boolean isInternetProtocol(final String string) {
        if (StringUtils.isEmpty(string)) {
            return Boolean.FALSE;
        }
        return PROTOCOL_PATTERN.matcher(string).matches();
    }

    public static String stripJSessionId(final String string, final String replacement) {
        if (StringUtils.isEmpty(string)) {
            return string;
        }
        return JSESSIONID_PATTERN.matcher(string).replaceAll(replacement);
    }

    public static String stripAnchor(final String string, final String replacement) {
        if (StringUtils.isEmpty(string)) {
            return string;
        }
        return ANCHOR_PATTERN.matcher(string).replaceAll(replacement);
    }

    public static String stripBlanks(final String string) {
        if (StringUtils.isEmpty(string)) {
            return string;
        }
        StringBuilder builder = new StringBuilder();
        char[] chars = string.toCharArray();
        for (char c : chars) {
            if (Character.isWhitespace(c) || Character.isSpaceChar(c)) {
                continue;
            }
            builder.append(c);
        }
        return builder.toString();
    }

    public static String stripCarriageReturn(final String string) {
        if (StringUtils.isEmpty(string)) {
            return string;
        }
        return CARRIAGE_LINE_FEED_PATTERN.matcher(string).replaceAll("");
    }

    public static String buildUri(final String protocol, final String host, final int port, final String path) {
        try {
            URL url = new URL(protocol, host, port, path);
            return url.toString();
        } catch (Exception e) {
            LOGGER.error("Exception building the url : " + protocol + ", " + host + ", " + port + ", " + path, e);
        }
        return null;
    }

    /**
     * This method will get the ip address of the machine. If the machine is connected to the net then the first ip
     * that is not the home
     * interface, i.e. not the localhost which is not particularly useful in a grid. So essentially we are looking
     * for the ip that looks
     * like 192.... or 10.215.... could be the real ip from the DNS on the ISP servers of course, but not 127.0.0.1,
     * or on Linux 127.0.1.1
     * it turns out.
     *
     * @return the first ip address that is not the localhost, something meaningful
     */
    public static String getIp() {
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            LOGGER.warn("No interfaces? Connected to anything?", e);
            throw new RuntimeException("Couldn't access the interfaces of this machine : ");
        }
        String ip = "127.0.0.1";
        String linuxIp = "127.0.1.1";
        String localhost = "localhost";
        // This is the preferred ip address for the machine
        String networkAssignedIp = "192.168.1.";
        outer:
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                String hostAddress = inetAddress.getHostAddress();
                if (hostAddress.equals(ip) || hostAddress.equals(linuxIp) || hostAddress.equals(localhost)) {
                    // If the ip address is localhost then just continue
                    continue;
                }
                try {
                    if (!isReachable(inetAddress, 1000)) {
                        continue;
                    }
                    if (hostAddress.startsWith(networkAssignedIp)) {
                        // The preferred ip address
                        ip = hostAddress;
                        break outer;
                    } else if (IP_PATTERN.matcher(hostAddress).matches()) {
                        ip = hostAddress;
                    }
                } catch (IOException e) {
                    LOGGER.error("Exception checking the ip address : " + hostAddress, e);
                }
            }
        }
        return ip;
    }

    private static boolean isReachable(final InetAddress inetAddress, final int timeout) throws IOException {
        return inetAddress.isReachable(timeout);
    }

}