# Public Key Pinning Using TrustKit
OstSdk uses [TrustKit v1.1.2](https://github.com/datatheorem/TrustKit-Android/tree/1.1.2) for public key pinning. 
If your application also uses `TrustKit`, the application may crash. This happens because `  TrustKit.initializeWithNetworkSecurityConfiguration` can be called only once during the application life-cycle. 
Please read through this document so that both application and sdk can use TrustKit.
</br>

## Setup TrustKit
Define your application's `network_security_config` file. 
> Do not use `ost_network_security_config` as file name. Please use a different name.

## Add `api.ost.com` domain-config to `network_security_config`
```xml
<!-- res/xml/network_security_config.xml -->
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- Application Pinning Policy Begin -->
    
    <!-- Application Pinning Policy End -->
    
    <!-- Pinning Policy of OstSdk -->
    <domain-config>
        <domain includeSubdomains="false">api.ost.com</domain>
        <pin-set>
            <pin digest="SHA-256">s4vrk6by0cqKQ9p/mFOakoi0daivc7Le8q7fUuuo4/U=</pin>
            <pin digest="SHA-256">MvVeCJ2tAuJZmbqoXMqSNP2mKh+VjGiljvqWytjzasU=</pin>
            <pin digest="SHA-256">J+0IGhy08mkHR1Z1WbdrHEdHhXRohrdLHUYORlWGafA=</pin>
            <pin digest="SHA-256">aF+lKYb0WChlCTx5uPBw5ZWze/98vAXSzBBIrVSZWJE=</pin>
            <pin digest="SHA-256">efgWbb0q/zHFLub1SY5QpoQVlZp33QpLOj0EmhoK8tI=</pin>
        </pin-set>
        <trustkit-config enforcePinning="true">
        </trustkit-config>
    </domain-config>
</network-security-config>
```

## Add the `networkSecurityConfig` to the application's `Manifest` file
To resolve networkSecurityConfig error</br>
```java
java.lang.RuntimeException: Manifest merger failed : Attribute application@networkSecurityConfig value=(@xml/network_security_config) from AndroidManifest.xml:25:9-69
	is also present at [:ostsdk] AndroidManifest.xml:26:18-82 value=(@xml/ost_network_security_config).
	Suggestion: add 'tools:replace="android:networkSecurityConfig"' to <application> element at AndroidManifest.xml:17:5-55:19 to override.
```
App must add `tools:replace="android:networkSecurityConfig"` in mainfest:
```
<?xml version="1.0" encoding="utf-8"?>
<manifest ... >
    <application android:networkSecurityConfig="@xml/network_security_config"
    tools:replace="android:networkSecurityConfig"
                    ... >
        ...
    </application>
</manifest>
```

## Initialize TrustKit before OST Wallet SDK
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
  super.OnCreate(savedInstanceState);

  // Using the default path - res/xml/network_security_config.xml
  TrustKit.initializeWithNetworkSecurityConfiguration(this);

  // OR using a resource (TrustKit can't be initialized twice)
  TrustKit.initializeWithNetworkSecurityConfiguration(this, R.xml.network_security_config);
  
  // String BASE_URL = <OstPlatform Url>
  // Initalize OstSdk  
  OstWalletUI.initialize(getApplicationContext(), BASE_URL);
}
```
